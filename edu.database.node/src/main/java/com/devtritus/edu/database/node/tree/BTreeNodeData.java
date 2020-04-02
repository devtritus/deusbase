package com.devtritus.edu.database.node.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class BTreeNodeData {
    private int nodeId;
    private int level;

    private List<String> keys = new ArrayList<>();
    private List<List<Long>> values = new ArrayList<>();

    private List<Integer> childrenNodeIds = new ArrayList<>();
    private List<Integer> childrenPositions = new ArrayList<>();

    int getNodeId() {
        return nodeId;
    }

    void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    int getLevel() {
        return level;
    }

    void setLevel(int level) {
        this.level = level;
    }

    List<String> getKeys() {
        return keys;
    }

    void setKeys(List<String> keys) {
        this.keys = keys;
    }

    List<List<Long>> getValues() {
        return values;
    }

    void setValues(List<List<Long>> values) {
        this.values = values;
    }

    List<Integer> getChildrenNodeIds() {
        return childrenNodeIds;
    }

    void setChildrenNodeIds(List<Integer> childrenNodeIds) {
        this.childrenNodeIds = childrenNodeIds;
    }

    List<Integer> getChildrenPositions() {
        return childrenPositions;
    }

    void setChildrenPositions(List<Integer> childrenPositions) {
        this.childrenPositions = childrenPositions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BTreeNodeData that = (BTreeNodeData) o;
        return nodeId == that.nodeId &&
                level == that.level &&
                Objects.equals(keys, that.keys) &&
                Objects.equals(values, that.values) &&
                Objects.equals(childrenNodeIds, that.childrenNodeIds) &&
                Objects.equals(childrenPositions, that.childrenPositions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, level, keys, values, childrenNodeIds, childrenPositions);
    }

    @Override
    public String toString() {
        return "BTreeNodeData{" +
                "nodeId=" + nodeId +
                ", level=" + level +
                ", keys=" + keys +
                ", values=" + values +
                ", childrenNodeIds=" + childrenNodeIds +
                ", childrenPositions=" + childrenPositions +
                '}';
    }
}
