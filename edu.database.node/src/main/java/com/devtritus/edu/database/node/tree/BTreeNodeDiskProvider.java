package com.devtritus.edu.database.node.tree;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class BTreeNodeDiskProvider implements BTreeNodeProvider<BTreeNode, String, Long, Integer>  {
    private final File file;
    private final int m;
    private final int blockSize;
    private int lastPosition;
    private int lastNodeId;
    private int rootPosition;

    private BTreeNodeCache cache = new BTreeNodeCache(10000);
    private PathEntry<BTreeNode, String, Long, Integer> root;

    public BTreeNodeDiskProvider(int m, int blockSize, int lastPosition, int lastNodeId, File file) {
        this.m = m;
        this.blockSize = blockSize;
        this.lastPosition = lastPosition;
        this.lastNodeId = lastNodeId;
        this.file = file;
    }

    @Override
    public PathEntry<BTreeNode, String, Long, Integer> getRootNode() {
        return root;
    }

    @Override
    public void setRootNode(PathEntry<BTreeNode, String, Long, Integer> entry) {
        int nodePosition = entry.value;
        try(SeekableByteChannel channel = openWriteChannel()) {
            channel.position(8);
            ByteBuffer buffer = ByteBuffer.allocate(4).putInt(nodePosition);
            buffer.rewind();
            channel.write(buffer);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        rootPosition = nodePosition;
        root = entry;
    }

    @Override
    public BTreeNode getChildNode(BTreeNode parentNode, int index) {
        List<Integer> children = parentNode.getChildren();

        if(index < 0 || index >= children.size()) {
            return null;
        }

        int nodePosition = parentNode.getChildren().get(index);
        BTreeNode cachedChildNode = cache.get(nodePosition);

        if(cachedChildNode != null) {
            return cachedChildNode;
        } else {
            BTreeNode childNode = getNodeByPosition(nodePosition);
            cache.put(nodePosition, childNode);
            return childNode;
        }
    }

    @Override
    public PathEntry<BTreeNode, String, Long, Integer> createNode(int level) {
        BTreeNode node = new BTreeNode(++lastNodeId, level);
        ++lastPosition;
        cache.put(lastPosition, node);
        return new PathEntry<>(node, lastPosition);
    }

    @Override
    public void insertChildNode(BTreeNode parentNode, PathEntry<BTreeNode, String, Long, Integer> newChildNode, int index) {
        parentNode.insertChildNode(index, newChildNode.value);
    }

    @Override
    public void flush() {
        Map<Integer, BTreeNode> modifiedNodes = cache.getModifiedNodes();

        try(SeekableByteChannel channel = openWriteChannel()) {
            for(Map.Entry<Integer, BTreeNode> entry : modifiedNodes.entrySet()) {
                int position = entry.getKey();
                byte[] bytes = BTreeNodeBytesConverter.toBytes(entry.getValue());
                ByteBuffer blockBuffer = ByteBuffer.allocate(blockSize).put(bytes);
                blockBuffer.rewind();
                channel.position(position * blockSize);
                channel.write(blockBuffer);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for(BTreeNode modifiedNode : modifiedNodes.values()) {
            modifiedNode.markAsNotModified();
        }

        updateHeader();
    }

    @Override
    public List<BTreeNode> getNodes(List<Integer> nodePositions) {
        List<BTreeNode> result = new ArrayList<>();
        for(Integer nodePosition : nodePositions) {
            BTreeNode node = cache.get(nodePosition);
            if(node != null) {
                result.add(node);
            } else {
                result.add(getNodeByPosition(nodePosition));
            }

        }
        return result;
    }

    void loadRoot(int rootPosition) {
        BTreeNode node = getNodeByPosition(rootPosition);
        root = new PathEntry<>(node, rootPosition);
        this.rootPosition = rootPosition;
        cache.put(rootPosition, node);
    }

    BTreeNode getNodeByPosition(int nodePosition) {
        try(SeekableByteChannel channel = openReadChannel()) {
            ByteBuffer blockBuffer = ByteBuffer.allocate(blockSize);
            channel.position(nodePosition * blockSize);
            channel.read(blockBuffer);
            blockBuffer.flip();
            return BTreeNodeBytesConverter.fromBytes(blockBuffer.array());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SeekableByteChannel openReadChannel() throws IOException {
        return Files.newByteChannel(file.toPath(), StandardOpenOption.READ);
    }

    private SeekableByteChannel openWriteChannel() throws IOException {
        return Files.newByteChannel(file.toPath(), StandardOpenOption.WRITE);
    }

    private void updateHeader() {
        try(SeekableByteChannel channel = openWriteChannel()) {
            ByteBuffer blockBuffer = ByteBuffer.allocate(blockSize);

            blockBuffer.putInt(blockSize)
                    .putInt(m)
                    .putInt(rootPosition)
                    .putInt(lastPosition)
                    .putInt(lastNodeId)
                    .rewind();

            channel.write(blockBuffer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void clearCache() {
        cache.clear();
        loadRoot(rootPosition);
    }
}
