package com.devtritus.edu.database.node.tree;

public class PathEntry extends Entry<BTreeNode<String, Long>, Integer> {
    PathEntry(BTreeNode<String, Long> key, Integer value) {
        super(key, value);
    }
}
