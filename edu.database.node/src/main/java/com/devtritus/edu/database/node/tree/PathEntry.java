package com.devtritus.edu.database.node.tree;

import com.devtritus.edu.database.node.utils.Pair;

class PathEntry<D extends AbstractBTreeNode<K, V, C>, K extends Comparable<K>, V, C> extends Pair<D, Integer> {
    PathEntry(D key, Integer value) {
        super(key, value);
    }
}
