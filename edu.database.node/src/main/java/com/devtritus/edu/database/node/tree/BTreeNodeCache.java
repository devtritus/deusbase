package com.devtritus.edu.database.node.tree;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class BTreeNodeCache {
    private final Map<Integer, BTreeNode> nodePositionToNode = new HashMap<>();
    private LinkedList<BTreeNode> queue;

    private final int limit;

    public BTreeNodeCache(int limit) {
        this.limit = limit;
    }

    public void put(int position, BTreeNode node) {
    }

    public void getByNodeId(int nodeId) {
    }

    public void getByPosition(int position) {

    }
}
