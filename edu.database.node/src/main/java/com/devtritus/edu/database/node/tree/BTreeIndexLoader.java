package com.devtritus.edu.database.node.tree;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

class BTreeIndexLoader {
    private final static int MIN_BLOCK_BYTE_SIZE = 512;

    private final File file;
    private BTreeIndexHeader header;

    private BTreeIndexLoader(File file, BTreeIndexHeader header) {
        this.file = file;
        this.header = header;
    }

    static BTreeIndexLoader init(int m, File file) throws IOException {
        int blockSize = calculateBlockSize(m);

        try(SeekableByteChannel channel = openWriteChannel(file)) {

            BTreeIndexHeader header = new BTreeIndexHeader(blockSize, m, 1, 1, 1);
            updateHeader(channel, header);

            BTreeNode root = new BTreeNode(header.lastNodeId, 0);
            ByteBuffer blockBuffer = ByteBuffer.allocate(blockSize);
            writeNode(channel, root, blockSize, 1, blockBuffer);

            return new BTreeIndexLoader(file, header);
        }
    }

    static BTreeIndexLoader read(File file) throws IOException {
        BTreeIndexHeader header = readHeader(file);

        return new BTreeIndexLoader(file, header);
    }

    int getLastPosition() {
        return header.lastPosition;
    }

    int getLastNodeId() {
        return header.lastNodeId;
    }

    int getM() {
        return header.m;
    }

    void flush(Map<Integer, BTreeNode> nodesToFlush, int rootPosition, int lastPosition, int lastNodeId) {
        int blockSize = header.blockSize;

        try(SeekableByteChannel channel = openWriteChannel(file)) {
            ByteBuffer blockBuffer = ByteBuffer.allocate(blockSize);
            for(Map.Entry<Integer, BTreeNode> entry : nodesToFlush.entrySet()) {
                writeNode(channel, entry.getValue(), blockSize, entry.getKey(), blockBuffer);
                blockBuffer.clear();
            }

            BTreeIndexHeader updatedHeader = new BTreeIndexHeader(blockSize, header.m, rootPosition, lastPosition, lastNodeId);
            if(!updatedHeader.equals(header)) {
                channel.position(0);
                updateHeader(channel, updatedHeader);
                header = updatedHeader;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    PathEntry<BTreeNode, String, List<Long>, Integer> readRoot() {
        BTreeNode node = readNodeByPosition(header.rootPosition);
        return new PathEntry<>(node, header.rootPosition);
    }

    BTreeNode readNodeByPosition(int position) {
        try(SeekableByteChannel channel = openReadChannel(file)) {
            ByteBuffer blockBuffer = ByteBuffer.allocate(header.blockSize);
            channel.position(position * header.blockSize);
            channel.read(blockBuffer);
            blockBuffer.flip();
            return BTreeNodeBytesConverter.fromBytes(blockBuffer.array());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeNode(SeekableByteChannel channel,  BTreeNode node, int blockSize, int position, ByteBuffer blockBuffer) throws IOException {
        byte[] bytes = BTreeNodeBytesConverter.toBytes(node);

        blockBuffer
                .put(bytes)
                .rewind();

        channel.position(position * blockSize);
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
                .putInt(header.lastPosition)
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
        int minBlockSize = (32 + 12) * m;
        int blockSize = MIN_BLOCK_BYTE_SIZE;
        while((blockSize = blockSize * 2) < minBlockSize) {}
        return blockSize;
    }
}
