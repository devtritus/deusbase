package com.devtritus.edu.database.node.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BTreeNodeData {
    private int nodeId;
    private int level;

    private List<String> keys = new ArrayList<>();
    private List<List<Long>> values = new ArrayList<>();

    private List<Integer> childrenNodeIds = new ArrayList<>();
    private List<Integer> childrenPositions = new ArrayList<>();

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public List<List<Long>> getValues() {
        return values;
    }

    public void setValues(List<List<Long>> values) {
        this.values = values;
    }

    public List<Integer> getChildrenNodeIds() {
        return childrenNodeIds;
    }

    public void setChildrenNodeIds(List<Integer> childrenNodeIds) {
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
