package com.devtritus.edu.database.node.tree;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BTreeNodeDiskProvider implements BTreeNodeProvider<BTreeNode, String, Long, Integer>  {
    private final Map<Integer, Integer> nodeIdToNodePosition = new HashMap<>();
    private final Map<Integer, BTreeNode> nodePositionToNode = new HashMap<>();

    private final File file;
    private final int m;
    private final int blockSize;
    private int lastPosition;
    private int lastNodeId;
    private int rootPosition;

    private BTreeNode root;

    public BTreeNodeDiskProvider(int m, int blockSize, int lastPosition, int lastNodeId, File file) {
        this.m = m;
        this.blockSize = blockSize;
        this.lastPosition = lastPosition;
        this.lastNodeId = lastNodeId;
        this.file = file;
    }

    @Override
    public BTreeNode getRootNode() {
        return root;
    }

    @Override
    public void setRootNode(BTreeNode node) {
        int nodePosition = nodeIdToNodePosition.get(node.getNodeId());
        try(SeekableByteChannel channel = openWriteChannel()) {
            channel.position(8);
            ByteBuffer buffer = ByteBuffer.allocate(4).putInt(nodePosition);
            buffer.rewind();
            channel.write(buffer);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        rootPosition = nodePosition;
        root = node;

        updateHeader();
    }

    @Override
    public BTreeNode getChildNode(BTreeNode parentNode, int index) {
        List<Integer> children = parentNode.getChildren();

        if(index < 0 || index >= children.size()) {
            return null;
        }

        int nodePosition = parentNode.getChildren().get(index);
        BTreeNode cachedChildNode = nodePositionToNode.get(nodePosition);

        if(cachedChildNode != null) {
            return cachedChildNode;
        } else {
            BTreeNode childNode = getNodeByPosition(nodePosition);
            putToMap(childNode, nodePosition);
            return childNode;
        }
    }

    @Override
    public BTreeNode createNode(int level) {
        BTreeNode node = new BTreeNode(++lastNodeId, level);
        putToMap(node, ++lastPosition);
        return node;
    }

    @Override
    public void insertChildNode(BTreeNode parentNode, BTreeNode newChildNode, int index) {
        Integer position = nodeIdToNodePosition.get(newChildNode.getNodeId());
        parentNode.insertChildNode(index, position);
    }

    @Override
    public void flush() {
        List<BTreeNode> modifiedNodes = nodePositionToNode.values().stream()
                .filter(BTreeNode::isModified)
                .collect(Collectors.toList());

        try(SeekableByteChannel channel = openWriteChannel()) {
            for(BTreeNode modifiedNode : modifiedNodes) {
                int position = nodeIdToNodePosition.get(modifiedNode.getNodeId());
                byte[] bytes = BTreeNodeBytesConverter.toBytes(modifiedNode);
                ByteBuffer blockBuffer = ByteBuffer.allocate(blockSize).put(bytes);
                blockBuffer.rewind();
                channel.position(position * blockSize);
                channel.write(blockBuffer);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for(BTreeNode modifiedNode : modifiedNodes) {
            modifiedNode.markAsNotModified();
        }

        updateHeader();
    }

    @Override
    public List<BTreeNode> getNodes(List<Integer> nodePositions) {
        List<BTreeNode> result = new ArrayList<>();
        for(Integer nodePosition : nodePositions) {
            BTreeNode node = nodePositionToNode.get(nodePosition);
            if(node != null) {
                result.add(node);
            } else {
                result.add(getNodeByPosition(nodePosition));
            }

        }
        return result;
    }

    void loadRoot(int rootPosition) {
        root = getNodeByPosition(rootPosition);
        this.rootPosition = rootPosition;
        putToMap(root, rootPosition);
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

    private void putToMap(BTreeNode node, int nodePosition) {
        nodeIdToNodePosition.put(node.getNodeId(), nodePosition);
        nodePositionToNode.put(nodePosition, node);
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
        nodePositionToNode.clear();
        nodeIdToNodePosition.clear();
        loadRoot(rootPosition);
    }
}
