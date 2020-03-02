package com.devtritus.edu.database.node.tree;

import java.util.List;

interface BTreeNodeProvider<D extends GenericBTreeNode<K, V, C>, K extends Comparable<K>, V, C> {
    D getRootNode();
    void setRootNode(D node);
    D getChildNode(D parentNode, int index);
    D createNode(int level);
    void insertChildNode(D parentNode, D newChildNode, int index);
    void flush();
    List<BTreeNode> getNodes(List<Integer> nodePositions);
}
