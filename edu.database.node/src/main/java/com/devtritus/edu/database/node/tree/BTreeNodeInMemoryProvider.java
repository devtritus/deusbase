package com.devtritus.edu.database.node.tree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BTreeNodeInMemoryProvider implements BTreeNodeProvider<BTreeNode, String, Integer, Integer> {
    private static int nodePositionCounter = 0;

    //for in-memory provider nodeId is same as nodePosition
    private final Map<Integer, BTreeNode> nodePositionToNodeMap = new HashMap<>();

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
        int nodePosition = parentNode.getChildren().get(index);
        return nodePositionToNodeMap.get(nodePosition);
    }

    @Override
    public BTreeNode createNode(int level) {
        int nodePosition = nodePositionCounter++;
        BTreeNode node = new BTreeNode(nodePosition, level);
        nodePositionToNodeMap.put(nodePosition, node);
        return node;
    }

    @Override
    public void insertChildNode(BTreeNode parentNode, BTreeNode newChildNode, int index) {
        parentNode.insertChildNode(index, newChildNode.getNodeId());
    }

    @Override
    public void flush() {
        List<BTreeNode> modifiedNodes = nodePositionToNodeMap.values().stream()
                .filter(GenericBTreeNode::isModified)
                .collect(Collectors.toList());

        //System.out.println("List of nodes to flush: " + modifiedNodes);

        //emulate flushing
        for(BTreeNode modifiedNode : modifiedNodes) {
            modifiedNode.markAsNotModified();
        }
    }

    @Override
    public List<BTreeNode> getNodes(List<Integer> nodePositions) {
        return nodePositions.stream()
                .map(nodePositionToNodeMap::get)
                .collect(Collectors.toList());
    }
}
