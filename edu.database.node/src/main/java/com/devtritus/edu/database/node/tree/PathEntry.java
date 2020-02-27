package com.devtritus.edu.database.node.tree;

class PathEntry<D extends GenericBTreeNode<K, V, C>, K extends Comparable<K>, V, C> extends Entry<D, Integer> {
    PathEntry(D key, Integer value) {
        super(key, value);
    }
}
