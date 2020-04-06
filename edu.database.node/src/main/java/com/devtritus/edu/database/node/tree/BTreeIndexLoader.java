package com.devtritus.edu.database.node.tree;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

class BTreeIndexLoader {
    private final static int MIN_BLOCK_BYTE_SIZE = 512;

    private final File file;
    private final Map<Integer, BTreeNodeMetadata> nodeIdToMetadata = new HashMap<>();

    private BTreeIndexHeader flushedHeader;
    private int lastNodeId;

    private BTreeIndexLoader(File file, BTreeIndexHeader header) {
        this.file = file;
        this.flushedHeader = header;
        this.lastNodeId = flushedHeader.lastNodeId;
    }

    static BTreeIndexLoader initIndex(int m, File file) throws IOException {
        if(m > 255) {
            throw new IllegalArgumentException("Size of keys can't be more then byte(255). m: " + m);
        }

        int blockSize = calculateBlockSize(m);

        try(SeekableByteChannel channel = openWriteChannel(file)) {

            BTreeIndexHeader header = new BTreeIndexHeader(blockSize, m, 1, 2, 1);
            updateHeader(channel, header);

            BTreeNodeData rootData = new BTreeNodeData();
            rootData.setNodeId(header.lastNodeId);
            rootData.setLevel(0);

            ByteBuffer blockBuffer = ByteBuffer.allocate(blockSize);
            byte[] bytes = BTreeNodeDataConverter.toBytes(rootData);
            writeNode(channel, bytes, blockSize, 1, blockBuffer);

            return new BTreeIndexLoader(file, header);
        }
    }

    static BTreeIndexLoader readIndex(File file) throws IOException {
        BTreeIndexHeader header = readHeader(file);

        return new BTreeIndexLoader(file, header);
    }

    int getM() {
        return flushedHeader.m;
    }

    BTreeNodeData createNode(int level) {
        ++lastNodeId;
        BTreeNodeData data = new BTreeNodeData();
        data.setNodeId(lastNodeId);
        data.setLevel(level);

        return data;
    }

    BTreeNodeData getRoot() {
        int flushedRootPosition = flushedHeader.rootPosition;
        return readNodeByPosition(flushedRootPosition);
    }

    void flush(List<BTreeNodeData> nodesToFlush, int rootNodeId) {
        final int blockSize = flushedHeader.blockSize;

        List<BTreeNodeData> sortedNodesToFlush = nodesToFlush.stream()
                .sorted(Comparator.comparingInt(BTreeNodeData::getLevel))
                .collect(Collectors.toList());

        try(SeekableByteChannel channel = openWriteChannel(file)) {

            ByteBuffer singleBlockBuffer = ByteBuffer.allocate(blockSize);

            int endPosition = flushedHeader.endPosition;
            for(BTreeNodeData data : sortedNodesToFlush) {
                List<Integer> childrenPosition = new ArrayList<>();
                for(Integer childNodeId : data.getChildrenNodeIds()) {
                    BTreeNodeMetadata metadata = nodeIdToMetadata.get(childNodeId);
                    childrenPosition.add(metadata.getPosition());
                }

                data.setChildrenPositions(childrenPosition);

                byte[] bytes = BTreeNodeDataConverter.toBytes(data);

                int dataSize = 1 + bytes.length; //blocksCount + data

                ByteBuffer dataBuffer;
                int nextBlocksCount;
                if(dataSize <= blockSize) {
                    nextBlocksCount = 1;
                    dataBuffer = singleBlockBuffer;
                } else {
                    nextBlocksCount = (dataSize / blockSize + 1);
                    dataBuffer = ByteBuffer.allocate(nextBlocksCount * blockSize);
                }

                BTreeNodeMetadata metadata = nodeIdToMetadata.get(data.getNodeId());

                int nextPosition;
               if(metadata != null) {
                   if(metadata.getBlockCount() < nextBlocksCount) {
                        metadata.setBlocksCount(nextBlocksCount);
                        metadata.setPosition(endPosition);
                        nextPosition = endPosition;
                        endPosition = endPosition + nextBlocksCount;
                    } else {
                        nextPosition = metadata.getPosition();
                    }
                } else {
                    BTreeNodeMetadata newMetadata = new BTreeNodeMetadata();
                    newMetadata.setPosition(endPosition);
                    newMetadata.setBlocksCount(nextBlocksCount);
                    nodeIdToMetadata.put(data.getNodeId(), newMetadata);
                    nextPosition = endPosition;
                    endPosition = endPosition + nextBlocksCount;
                }

                writeNode(channel, bytes,  nextPosition * blockSize, nextBlocksCount, dataBuffer);

                dataBuffer.clear();
            }

            BTreeNodeMetadata rootMetadata = nodeIdToMetadata.get(rootNodeId);
            int rootPosition = rootMetadata.getPosition();

            BTreeIndexHeader updatedHeader = new BTreeIndexHeader(blockSize, flushedHeader.m, rootPosition, endPosition, lastNodeId);
            if(!updatedHeader.equals(flushedHeader)) {
                channel.position(0);
                updateHeader(channel, updatedHeader);

                flushedHeader = updatedHeader;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    BTreeNodeData readNodeByNodeId(int nodeId) {
        BTreeNodeMetadata metadata = nodeIdToMetadata.get(nodeId);
        return readNodeByPosition(metadata.getPosition());
    }

    Map<Integer, BTreeNodeData> readAll() {
        Map<Integer, BTreeNodeData> result = new HashMap<>();
        BTreeNodeData root = readNodeByPosition(flushedHeader.rootPosition);
        result.put(root.getNodeId(), root);
        readAll(root, result);

        return result;
    }

    private void readAll(BTreeNodeData root, Map<Integer, BTreeNodeData> result) {
        for(int i = 0; i < root.getChildrenNodeIds().size(); i++) {
            BTreeNodeData data = readNodeByPosition(root.getChildrenPositions().get(i));
            result.put(data.getNodeId(), data);
            readAll(data, result);
        }
    }

    private BTreeNodeData readNodeByPosition(int position) {
        try(SeekableByteChannel channel = openReadChannel(file)) {
            ByteBuffer firstBlockBuffer = ByteBuffer.allocate(flushedHeader.blockSize);
            channel.position(position * flushedHeader.blockSize);
            channel.read(firstBlockBuffer);
            firstBlockBuffer.flip();
            int blocksCount = firstBlockBuffer.get();

            ByteBuffer nodeBuffer;
            if(blocksCount == 1) {
                nodeBuffer = firstBlockBuffer;
            } else {
                channel.position(position * flushedHeader.blockSize);
                nodeBuffer = ByteBuffer.allocate(blocksCount * flushedHeader.blockSize);
                channel.read(nodeBuffer);
                nodeBuffer.flip();
                nodeBuffer.get(); //repeatable reading for blocksCount
            }

            byte[] nodeBytes = new byte[nodeBuffer.remaining()];
            nodeBuffer.get(nodeBytes);

            BTreeNodeData data = BTreeNodeDataConverter.fromBytes(nodeBytes);

            BTreeNodeMetadata metadata = nodeIdToMetadata.get(data.getNodeId());
            if(metadata == null) {
                metadata = new BTreeNodeMetadata();
                nodeIdToMetadata.put(data.getNodeId(), metadata);
            }

            metadata.setBlocksCount(blocksCount);
            metadata.setPosition(position);

            List<Integer> childrenNodeIds = data.getChildrenNodeIds();
            List<Integer> childrenPositions = data.getChildrenPositions();
            int childrenSize = childrenNodeIds.size();
            for(int i = 0; i < childrenSize; i++) {
                Integer childNodeId = childrenNodeIds.get(i);
                Integer childPosition = childrenPositions.get(i);

                BTreeNodeMetadata childMetadata = nodeIdToMetadata.get(childNodeId);
                if(childMetadata == null) {
                    childMetadata = new BTreeNodeMetadata();
                    childMetadata.setPosition(childPosition);

                    nodeIdToMetadata.put(childNodeId, childMetadata);
                }
            }

            return data;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeNode(SeekableByteChannel channel, byte[] bytes, int startPosition, int blocksCount, ByteBuffer blockBuffer) throws IOException {
        blockBuffer
                .put((byte)blocksCount)
                .put(bytes)
                .rewind();

        channel.position(startPosition);
        channel.write(blockBuffer);
    }

    private static BTreeIndexHeader readHeader(File file) throws IOException {
        try (SeekableByteChannel channel = openReadChannel(file)) {
            ByteBuffer headerBuffer = ByteBuffer.allocate(20);
            channel.read(headerBuffer);
            headerBuffer.flip();

            return new BTreeIndexHeader(
                    headerBuffer.getInt(),
                    headerBuffer.getInt(),
                    headerBuffer.getInt(),
                    headerBuffer.getInt(),
                    headerBuffer.getInt()
            );
        }
    }

    private static void updateHeader(SeekableByteChannel channel, BTreeIndexHeader header) throws IOException {
        ByteBuffer blockBuffer = ByteBuffer.allocate(header.blockSize);

        blockBuffer
                .putInt(header.blockSize)
                .putInt(header.m)
                .putInt(header.rootPosition)
                .putInt(header.endPosition)
                .putInt(header.lastNodeId)
                .rewind();

        channel.write(blockBuffer);
    }

    private static SeekableByteChannel openReadChannel(File file) throws IOException {
        return Files.newByteChannel(file.toPath(), StandardOpenOption.READ);
    }

    private static SeekableByteChannel openWriteChannel(File file) throws IOException {
        return Files.newByteChannel(file.toPath(), StandardOpenOption.WRITE);
    }

    private static int calculateBlockSize(int m) {
        int minBlockSize = (32 + 10) * m;
        int blockSize = MIN_BLOCK_BYTE_SIZE;
        while((blockSize = blockSize * 2) < minBlockSize) {}
        return blockSize;
    }
}
