package com.devtritus.edu.database.node.tree;

import java.util.*;
import static com.devtritus.edu.database.node.tree.BTreeNodeConverter.*;

class BTreeNodeDiskProvider implements BTreeNodeProvider<BTreeNode, String, List<Long>, Integer>  {
    private final BTreeIndexLoader loader;
    private final BTreeNodeCache cache;

    private BTreeNode root;

    BTreeNodeDiskProvider(BTreeIndexLoader loader, BTreeNodeCache cache) {
        this.loader = loader;
        this.cache = cache;
    }

    @Override
    public BTreeNode getRootNode() {
        if(root == null) {
            root = fromNodeData(loader.getRoot());
            root.setRoot(true);
            cache.put(root);
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
        BTreeNode cachedChildNode = cache.get(nodeId);

        if(cachedChildNode != null) {
            return cachedChildNode;
        } else {
            BTreeNode childNode = fromNodeData(loader.readNodeByNodeId(nodeId));
            cache.put(childNode);
            return childNode;
        }
    }

    @Override
    public BTreeNode createNode(int level) {
        BTreeNode node = fromNodeData(loader.createNode(level));
        cache.put(node);
        return node;
    }

    @Override
    public void putKeyValueToNode(BTreeNode node, int index, String key, List<Long> value) {
        node.putKeyValue(key, value);
        cache.put(node);
    }

    @Override
    public void insertChildNode(BTreeNode parentNode, BTreeNode newChildNode, int index) {
        parentNode.insertChild(index, newChildNode.getNodeId());
    }

    @Override
    public void flush() {
        List<BTreeNode> modifiedNodes = cache.getModifiedNodes();

        BTreeNode updatedRootNode = null;
        List<BTreeNodeData> modifiesNodeDataList = new ArrayList<>();
        for(BTreeNode node : modifiedNodes) {
            modifiesNodeDataList.add(toNodeData(node));
            if(node.isRoot()) {
                updatedRootNode = node;
            }
        }

        int rootNodeId;
        if(updatedRootNode != null) {
            root = updatedRootNode;
            rootNodeId = updatedRootNode.getNodeId();
        } else {
            rootNodeId = root.getNodeId();
        }

        loader.flush(modifiesNodeDataList, rootNodeId);

        for(BTreeNode modifiedNode : modifiedNodes) {
            modifiedNode.markAsNotModified();
        }

        cache.clearToLimit();
    }

    @Override
    public List<BTreeNode> getNodes(List<Integer> nodeIds) {
        List<BTreeNode> result = new ArrayList<>();
        for(Integer nodeId : nodeIds) {
            BTreeNode cachedNode = cache.get(nodeId);
            if(cachedNode != null) {
                result.add(cachedNode);
            } else {
                BTreeNode node = fromNodeData(loader.readNodeByNodeId(nodeId));
                result.add(node);
            }
        }
        return result;
    }

    void clearCache() {
        cache.clear();
    }
}
