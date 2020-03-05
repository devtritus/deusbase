package com.devtritus.edu.database.node.tree;

import java.util.*;
import java.util.stream.Collectors;

class BTreeNodeCache {
    private final static float C = 0.2f;

    private final Map<Integer, Integer> nodePositionToNodeIndex = new HashMap<>();
    private List<BTreeNode> nodes = new ArrayList<>();
    private int limit;

    BTreeNodeCache(int limit) {
        this.limit = limit;
    }

    void put(int position, BTreeNode node) {
        Integer oldNodeIndex = nodePositionToNodeIndex.get(position);
        nodes.add(node);
        int nodesSize = nodes.size();
        nodePositionToNodeIndex.put(position, nodesSize - 1);
        if(oldNodeIndex != null) {
            TreeUtils.delete(nodes, oldNodeIndex);
            for(Map.Entry<Integer, Integer> entry : nodePositionToNodeIndex.entrySet()) {
                if(entry.getValue() >= oldNodeIndex) {
                    nodePositionToNodeIndex.put(entry.getKey(), entry.getValue() - 1);
                }
            }

        } else if(nodes.size() > limit) {
            int n = Math.max((int)(nodes.size() * C), 1);
            nodes.subList(0, n).clear();

            List<Integer> toDelete = new ArrayList<>();
            for(Map.Entry<Integer, Integer> entry : nodePositionToNodeIndex.entrySet()) {
                if(entry.getValue() < n) {
                    toDelete.add(entry.getKey());
                } else {
                    nodePositionToNodeIndex.put(entry.getKey(), entry.getValue() - n);
                }
            }

            for(Integer positionToDelete : toDelete) {
                nodePositionToNodeIndex.remove(positionToDelete);
            }
        }
    }

    BTreeNode get(int position) {
        Integer nodeIndex = nodePositionToNodeIndex.get(position);
        if(nodeIndex == null) {
            return null;
        }
        return nodes.get(nodeIndex);
    }

    void clear() {
        nodes.clear();
        nodePositionToNodeIndex.clear();
    }

    Map<Integer, BTreeNode> getModifiedNodes() {
        return getCachedEntries().entrySet().stream()
                .filter(entry -> entry.getValue().isModified())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    Map<Integer, BTreeNode> getCachedEntries() {
        return nodePositionToNodeIndex.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> nodes.get(entry.getValue())));
    }
}
