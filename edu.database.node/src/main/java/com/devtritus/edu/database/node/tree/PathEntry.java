package com.devtritus.edu.database.node.tree;

class PathEntry<K extends Comparable<K>, V> extends Entry<BTreeNode<K, V>, Integer> {
    PathEntry(BTreeNode<K, V> key, Integer value) {
        super(key, value);
    }
}
