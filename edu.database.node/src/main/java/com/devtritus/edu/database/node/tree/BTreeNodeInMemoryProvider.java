package com.devtritus.edu.database.node.tree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BTreeNodeInMemoryProvider implements BTreeNodeProvider<BTreeNode, String, Integer, Integer> {
    private static int nodeIdCounter = 0;
    private static int position = 0;

    private final Map<Integer, Entry<BTreeNode, Integer>> nodeIdToEntry = new HashMap<>();

    private BTreeNode root;

    @Override
    public BTreeNode getRootNode() {
        if(root == null) {
            root = createNode(0);
        }
        return root;
    }

    @Override
    public void setRootNode(BTreeNode node) {
        root = node;
    }

    @Override
    public BTreeNode getChildNode(BTreeNode parentNode, int index) {
        List<Integer> children = parentNode.getChildren();

        if(index < 0 || index >= children.size()) {
            return null;
        }
        int nodeId = parentNode.getChildren().get(index);
        return nodeIdToEntry.get(nodeId).key; //TODO: it is not key. Rename fields
    }

    @Override
    public int getChildrenSize(BTreeNode parentNode) {
        return parentNode.getChildren().size();
    }

    @Override
    public int indexOfChildNode(BTreeNode parentNode, BTreeNode childNode) {
        int childNodeId = childNode.getNodeId();
        List<Integer> children = parentNode.getChildren();
        for(int i = 0; i < children.size(); i++) {
            if(childNodeId == children.get(i)) {
                return i;
            }
        }

        throw new IllegalStateException();
    }

    @Override
    public BTreeNode createNode(int level) {
        int nodeId = nodeIdCounter++;
        int nodePosition = position++;
        BTreeNode node = new BTreeNode(nodeId, level);
        nodeIdToEntry.put(nodeId, new Entry<>(node, nodePosition));
        return node;
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

    List<BTreeNode> getNodes(List<Integer> nodeIds) {
        return nodeIds.stream().map(nodeId -> nodeIdToEntry.get(nodeId).key).collect(Collectors.toList());
    }
}
