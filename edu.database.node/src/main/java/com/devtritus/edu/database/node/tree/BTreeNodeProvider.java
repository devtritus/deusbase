package com.devtritus.edu.database.node.tree;

import java.util.List;

interface BTreeNodeProvider<D extends GenericBTreeNode<K, V, C>, K extends Comparable<K>, V, C> {
    PathEntry<D, K, V, C> getRootNode();
    void setRootNode(PathEntry<D, K, V, C> node);
    D getChildNode(D parentNode, int index);
    PathEntry<D, K, V, C> createNode(int level);
    void insertChildNode(D parentNode, PathEntry<D, K, V, C> newChildNode, int index);
    void flush();
    List<BTreeNode> getNodes(List<Integer> nodePositions);
}
