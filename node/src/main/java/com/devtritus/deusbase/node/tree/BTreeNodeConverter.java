package com.devtritus.deusbase.node.tree;

import com.devtritus.deusbase.node.index.BTreeNodeData;

abstract class BTreeNodeConverter {

    static BTreeNodeData toNodeData(BTreeNode node) {
        BTreeNodeData data = new BTreeNodeData();

        data.setNodeId(node.getNodeId());
        data.setLevel(node.getLevel());
        data.setKeys(node.getKeys());
        data.setValues(node.getValues());
        data.setChildrenNodeIds(node.getChildren());

        return data;
    }

    static BTreeNode fromNodeData(BTreeNodeData data) {
        BTreeNode node = new BTreeNode(data.getNodeId(), data.getLevel());

        node.setKeys(data.getKeys());
        node.setValues(data.getValues());
        node.setChildren(data.getChildrenNodeIds());

        return node;
    }
}
