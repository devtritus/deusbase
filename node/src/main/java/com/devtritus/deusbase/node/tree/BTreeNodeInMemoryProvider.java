package com.devtritus.deusbase.node.tree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BTreeNodeInMemoryProvider implements BTreeNodeProvider<BTreeNode, String, List<Long>, Integer> {
    private static int nodeIdCounter = 0;

    private final Map<Integer, BTreeNode> nodeIdToNodeMap = new HashMap<>();

    private BTreeNode root;

    @Override
    public BTreeNode getRootNode() {
        if(root == null) {
            root = createNode(0);
        }
        return root;
    }

    @Override
    public BTreeNode getChildNode(BTreeNode parentNode, int index) {
        List<Integer> children = parentNode.getChildren();

        if(index < 0 || index >= children.size()) {
            return null;
        }
        int nodeId = parentNode.getChildren().get(index);
        return nodeIdToNodeMap.get(nodeId);
    }

    @Override
    public BTreeNode createNode(int level) {
        int nodeId = nodeIdCounter++;
        BTreeNode node = new BTreeNode(nodeId, level);
        nodeIdToNodeMap.put(nodeId, node);
        return node;
    }

    @Override
    public void putKeyValueToNode(BTreeNode node, int index, String key, List<Long> value) {
        node.putKeyValue(index, key, value);
        nodeIdToNodeMap.put(node.getNodeId(), node);
    }

    @Override
    public void insertChildNode(BTreeNode parentNode, BTreeNode newChildNode, int index) {
        parentNode.insertChild(index, newChildNode.getNodeId());
    }

    @Override
    public void flush() {
        List<BTreeNode> modifiedNodes = nodeIdToNodeMap.values().stream()
                .filter(AbstractBTreeNode::isModified)
                .collect(Collectors.toList());

        //System.out.println("List of nodes to flush: " + modifiedNodes);

        //emulate flushing
        for(BTreeNode modifiedNode : modifiedNodes) {
            if(modifiedNode.isRoot()) {
                root = modifiedNode;
            }
            modifiedNode.markAsNotModified();
        }
    }

    @Override
    public List<BTreeNode> getNodes(List<Integer> nodeIds) {
        return nodeIds.stream()
                .map(nodeIdToNodeMap::get)
                .collect(Collectors.toList());
    }
}
