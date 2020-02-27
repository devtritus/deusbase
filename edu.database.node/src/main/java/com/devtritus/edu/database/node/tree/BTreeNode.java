package com.devtritus.edu.database.node.tree;

class BTreeNode extends GenericBTreeNode<String, Integer, Integer> {
    BTreeNode(int nodeId, int level) {
        super(nodeId, level);
    }
}
