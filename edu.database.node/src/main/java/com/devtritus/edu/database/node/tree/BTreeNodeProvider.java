package com.devtritus.edu.database.node.tree;

interface BTreeNodeProvider<D extends GenericBTreeNode<K, V, C>, K extends Comparable<K>, V, C> {
    D getRootNode();
    void setRootNode(D node);
    D getChildNode(D parentNode, int index);
    int getChildrenSize(D parentNode);
    int indexOfChildNode(D parentNode, D childNode);
    D createNode(int level);
}
