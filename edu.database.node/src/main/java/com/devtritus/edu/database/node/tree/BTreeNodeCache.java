package com.devtritus.edu.database.node.tree;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

class BTreeNodeCache {
    private final Map<Integer, BTreeNode> nodePositionToNode = new HashMap<>();
    private LinkedList<BTreeNode> queue;

    private final int limit;

    BTreeNodeCache(int limit) {
        this.limit = limit;
    }

    void put(int position, BTreeNode node) {
        nodePositionToNode.put(position, node);
    }

    BTreeNode get(int position) {
        return nodePositionToNode.get(position);
    }

    void clear() {
        nodePositionToNode.clear();
    }

    Map<Integer, BTreeNode> getModifiedNodes() {
        return nodePositionToNode.entrySet().stream()
                .filter(entry -> entry.getValue().isModified())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
