package com.devtritus.edu.database.node.tree;

import java.util.List;

class BTreeNode extends AbstractBTreeNode<String, List<Long>, Integer> {
    BTreeNode(Integer nodeId, int level) {
        super(nodeId, level, false);
    }
}
