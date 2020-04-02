package com.devtritus.edu.database.node.tree;

import java.util.List;

interface BTreeNodeProvider<D extends AbstractBTreeNode<K, V, C>, K extends Comparable<K>, V, C> {
    D getRootNode();
    D getChildNode(D parentNode, int index);
    D createNode(int level);
    void putKeyValueToNode(D node, int index, K key, V value);
    void insertChildNode(D parentNode, D newChildNode, int index);
    void flush();
    List<BTreeNode> getNodes(List<Integer> nodePositions);
}
