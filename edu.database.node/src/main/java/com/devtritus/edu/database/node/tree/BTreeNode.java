package com.devtritus.edu.database.node.tree;

class BTreeNode extends GenericBTreeNode<String, Long, Integer> {
    BTreeNode(int nodeId, int level) {
        super(nodeId, level, false);
    }
}
