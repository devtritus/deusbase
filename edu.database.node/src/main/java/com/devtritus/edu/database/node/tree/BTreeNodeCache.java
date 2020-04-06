package com.devtritus.edu.database.node.tree;

import com.devtritus.edu.database.node.utils.Utils;
import java.util.*;
import java.util.stream.Collectors;

class BTreeNodeCache {
    private final static float C = 0.2f;

    private final Map<Integer, Integer> nodeIdToNodeIndex = new HashMap<>();
    private List<BTreeNode> nodes = new ArrayList<>();
    private int limit;

    BTreeNodeCache(int limit) {
        this.limit = limit;
    }

    void put(BTreeNode node) {
        int nodeId = node.getNodeId();
        Integer oldNodeIndex = nodeIdToNodeIndex.get(nodeId);
        nodes.add(node);
        int nodesSize = nodes.size();
        nodeIdToNodeIndex.put(nodeId, nodesSize - 1);
        if(oldNodeIndex != null) {
            Utils.deleteFromList(nodes, oldNodeIndex);
            for(Map.Entry<Integer, Integer> entry : nodeIdToNodeIndex.entrySet()) {
                if(entry.getValue() >= oldNodeIndex) {
                    nodeIdToNodeIndex.put(entry.getKey(), entry.getValue() - 1);
                }
            }
        }
    }

    BTreeNode get(int nodeId) {
        Integer nodeIndex = nodeIdToNodeIndex.get(nodeId);
        if(nodeIndex == null) {
            return null;
        }
        return nodes.get(nodeIndex);
    }

    void delete(int nodeId) {
        Integer nodeIndex = nodeIdToNodeIndex.remove(nodeId);
        if(nodeIndex == null) {
            throw new IllegalStateException();
        }
    }

    void clearToLimit() {
        if(nodes.size() > limit) {
            int n = Math.max((int)(nodes.size() * C), 1);
            nodes.subList(0, n).clear();

            List<Integer> toDelete = new ArrayList<>();
            for(Map.Entry<Integer, Integer> entry : nodeIdToNodeIndex.entrySet()) {
                if(entry.getValue() < n) {
                    toDelete.add(entry.getKey());
                } else {
                    nodeIdToNodeIndex.put(entry.getKey(), entry.getValue() - n);
                }
            }

            for(Integer nodeIdToDelete : toDelete) {
                nodeIdToNodeIndex.remove(nodeIdToDelete);
            }
        }
    }

    void clear() {
        nodes.clear();
        nodeIdToNodeIndex.clear();
    }

    List<BTreeNode> getModifiedNodes() {
        return getCachedNodes().values().stream()
                .filter(AbstractBTreeNode::isModified)
                .collect(Collectors.toList());
    }

    Map<Integer, BTreeNode> getCachedNodes() {
        return nodeIdToNodeIndex.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> nodes.get(entry.getValue())));
    }
}
