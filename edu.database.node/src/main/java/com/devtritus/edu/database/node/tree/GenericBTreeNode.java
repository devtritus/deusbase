package com.devtritus.edu.database.node.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class GenericBTreeNode<K extends Comparable<K>, V, C> {

    private final List<K> keys = new ArrayList<>();
    private final List<V> values = new ArrayList<>();
    private final List<C> children = new ArrayList<>();

    private final C nodeId;
    private final int level;

    GenericBTreeNode(C nodeId, int level) {
        this.nodeId = nodeId;
        this.level = level;
    }

    C getNodeId() {
        return nodeId;
    }

    boolean isLeaf() {
        return getLevel() == 0;
    }

    int getLevel() {
        return level;
    }

    List<K> getKeys() {
        return new ArrayList<>(keys);
    }

    List<V> getValues() {
        return new ArrayList<>(values);
    }

    List<C> getChildren() {
        return new ArrayList<>(children);
    }

    int getKeysSize() {
        return getKeys().size();
    }

    int searchKey(K key) {
        return Collections.binarySearch(keys, key);
    }

    Entry<K, V> getKeyValue(int index) {
        K key = keys.get(index);
        V value = values.get(index);
        return new Entry<>(key, value);
    }

    void putKeyValue(int index, K key, V value) {
        if(index > -1) {
            values.set(index, value);
        } else {
            int insertionIndex = -index - 1;
            insertKeyValue(insertionIndex, key, value);
        }
    }

    void insertKeyValue(int index, K key, V value) {
        TreeUtils.insert(keys, key, index);
        TreeUtils.insert(values, value, index);
    }

    Entry<K, V> deleteKeyValue(int index) {
        if(index < 0) {
            throw new IllegalStateException(String.format("Key by index %s not found", index));
        }

        Entry<K, V> keyValue = getKeyValue(index);

        keys.remove(index);
        values.remove(index);

        return keyValue;
    }

    void insertChildNode(int index, C child) {
        TreeUtils.insert(children, child, index);
    }

    C deleteChildNode(int index) {
        return children.remove(index);
    }

    void putKeyValue(K key, V value) {
        int index = searchKey(key);
        putKeyValue(index, key, value);
    }

    V getValue(K key) {
        int index = searchKey(key);
        if(index < 0) {
            return null;
        } else {
            return values.get(index);
        }
    }

    void delete(int start, int end) {
        keys.subList(start, end).clear();
        values.subList(start, end).clear();
        if(!children.isEmpty()) {
            children.subList(start, end + 1).clear();
        }
    }

    void copy(GenericBTreeNode<K, V, C> fromNode, int start, int end) {
        keys.addAll(fromNode.getKeys().subList(start, end));
        values.addAll(fromNode.getValues().subList(start, end));
        if(!fromNode.getChildren().isEmpty()) {
            children.addAll(fromNode.getChildren().subList(start, end + 1));
        }
    }

    @Override
    public String toString() {
        return String.format("node_%s(level=%s) keys=%s", nodeId, level, keys);
    }
}
