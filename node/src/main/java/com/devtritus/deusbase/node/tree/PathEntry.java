package com.devtritus.deusbase.node.tree;

import com.devtritus.deusbase.node.utils.Pair;

class PathEntry<D extends AbstractBTreeNode<K, V, C>, K extends Comparable<K>, V, C> extends Pair<D, Integer> {
    PathEntry(D key, Integer value) {
        super(key, value);
    }
}
