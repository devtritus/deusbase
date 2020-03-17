package com.devtritus.edu.database.node.tree;

import java.util.*;

class BTreeNodeDiskProvider implements BTreeNodeProvider<BTreeNode, String, List<Long>, Integer>  {
    private final BTreeIndexLoader loader;
    private final BTreeNodeCache cache;

    private PathEntry<BTreeNode, String, List<Long>, Integer> root;
    private int lastPosition;
    private int lastNodeId;

    private BTreeNodeDiskProvider(BTreeIndexLoader loader,
                                  BTreeNodeCache cache,
                                  PathEntry<BTreeNode, String, List<Long>, Integer> root,
                                  int lastPosition,
                                  int lastNodeId) {
        this.loader = loader;
        this.cache = cache;
        this.root = root;
        this.lastPosition = lastPosition;
        this.lastNodeId = lastNodeId;
    }

    static BTreeNodeDiskProvider create(BTreeIndexLoader loader, BTreeNodeCache cache) {
        return new BTreeNodeDiskProvider(loader, cache, loader.readRoot(), loader.getLastPosition(), loader.getLastNodeId());
    }

    @Override
    public PathEntry<BTreeNode, String, List<Long>, Integer> getRootNode() {
        return root;
    }

    @Override
    public void setRootNode(PathEntry<BTreeNode, String, List<Long>, Integer> entry) {
        root = entry;
        if(cache.get(entry.value) == null) {
            cache.put(entry.value, entry.key);
        }
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
            BTreeNode childNode = loader.readNodeByPosition(nodePosition);
            cache.put(nodePosition, childNode);
            return childNode;
        }
    }

    @Override
    public PathEntry<BTreeNode, String, List<Long>, Integer> createNode(int level) {
        BTreeNode node = new BTreeNode(++lastNodeId, level);
        ++lastPosition;
        cache.put(lastPosition, node);
        return new PathEntry<>(node, lastPosition);
    }

    @Override
    public void putKeyValueToNode(PathEntry<BTreeNode, String, List<Long>, Integer> entry, int index, String key, List<Long> value) {
        BTreeNode node = entry.key;
        int nodePosition = entry.value;

        node.putKeyValue(key, value);
        cache.put(nodePosition, node);
    }

    @Override
    public void insertChildNode(BTreeNode parentNode, PathEntry<BTreeNode, String, List<Long>, Integer> newChildNode, int index) {
        parentNode.insertChildNode(index, newChildNode.value);
    }

    @Override
    public void flush() {
        Map<Integer, BTreeNode> modifiedNodes = cache.getModifiedNodes();

        loader.flush(modifiedNodes, root.value, lastPosition, lastNodeId);

        cache.clearToLimit();

        for(BTreeNode modifiedNode : modifiedNodes.values()) {
            modifiedNode.markAsNotModified();
        }
    }

    @Override
    public List<BTreeNode> getNodes(List<Integer> nodePositions) {
        List<BTreeNode> result = new ArrayList<>();
        for(Integer nodePosition : nodePositions) {
            BTreeNode cachedNode = cache.get(nodePosition);
            if(cachedNode != null) {
                result.add(cachedNode);
            } else {
                BTreeNode node = loader.readNodeByPosition(nodePosition);
                result.add(node);
            }
        }
        return result;
    }

    void clearCache() {
        cache.clear();
        root = loader.readRoot();
    }
}
