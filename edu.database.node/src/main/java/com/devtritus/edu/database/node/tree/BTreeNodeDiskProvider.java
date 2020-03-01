package com.devtritus.edu.database.node.tree;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BTreeNodeDiskProvider implements BTreeNodeProvider<BTreeNode, String, Integer, Integer>  {
    private final Map<Integer, Entry<BTreeNode, Integer>> nodeIdToEntry = new HashMap<>();

    private final File file;
    private final int m;
    private final int blockSize;
    private int lastId;

    private BTreeNode root;

    public BTreeNodeDiskProvider(int m, int blockSize, int lastId, File file) {
        this.m = m;
        this.blockSize = blockSize;
        this.lastId = lastId;
        this.file = file;
    }

    @Override
    public BTreeNode getRootNode() {
        return root;
    }

    @Override
    public void setRootNode(BTreeNode node) {
        int nodeId = node.getNodeId();
        try(FileChannel fileChannel = openWriteChannel()) {
            fileChannel.position(12);
            fileChannel.write(ByteBuffer.allocate(4).putInt(nodeId));
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        root = node;
    }

    @Override
    public BTreeNode getChildNode(BTreeNode parentNode, int index) {
        List<Integer> children = parentNode.getChildren();

        if(index < 0 || index >= children.size()) {
            return null;
        }
        int nodeId = parentNode.getChildren().get(index);
        Entry<BTreeNode, Integer> cachedChild = nodeIdToEntry.get(nodeId);
        if(cachedChild != null) {
            return cachedChild.key;
        } else {
        }
        return null;
    }

    @Override
    public BTreeNode createNode(int level) {
/*        int nodeId = nodeIdCounter++;
        int nodePosition = position++;
        BTreeNode node = new BTreeNode(nodeId, level);
        nodeIdToEntry.put(nodeId, new Entry<>(node, nodePosition));
        return node;*/
        return null;
    }

    @Override
    public void flush() {
        List<BTreeNode> modifiedNodes = nodeIdToEntry.values().stream()
                .map(entry -> entry.key)
                .collect(Collectors.toList());

        //System.out.println("List of nodes to flush: " + modifiedNodes);

        //emulate flushing
        for(BTreeNode modifiedNode : modifiedNodes) {
            modifiedNode.markAsNotModified();
        }
    }

    void loadRoot(int rootPosition) {
        root = getNodeByPosition(rootPosition);
        putToMap(root, rootPosition);
    }

    BTreeNode getNodeByPosition(int position) {
        try(FileChannel fileChannel = openReadChannel()) {
            ByteBuffer blockBuffer = ByteBuffer.allocate(blockSize);
            fileChannel.position(position * blockSize);
            fileChannel.read(blockBuffer);
            blockBuffer.flip();
            return BTreeNodeBytesConverter.fromBytes(blockBuffer.array());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void writeNodeToPosition(BTreeNode node, int position) {
        try(FileChannel fileChannel = openWriteChannel()) {
            fileChannel.position(position * blockSize);
            byte[] bytes = BTreeNodeBytesConverter.toBytes(node);
            ByteBuffer blockBuffer = ByteBuffer.wrap(bytes);
            fileChannel.write(blockBuffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void putToMap(BTreeNode node, int nodePosition) {
        nodeIdToEntry.put(node.getNodeId(), new Entry<>(root, nodePosition));
    }

    private FileChannel openReadChannel() throws IOException {
        return new FileInputStream(file).getChannel();
    }

    private FileChannel openWriteChannel() throws IOException {
        return new FileOutputStream(file).getChannel();
    }
}
