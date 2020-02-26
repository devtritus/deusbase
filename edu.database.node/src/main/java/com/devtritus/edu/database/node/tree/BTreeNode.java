package com.devtritus.edu.database.node.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class BTreeNode<K extends Comparable<K>, V> {
    private static int counter = 0;
    private final int nodeId = counter++;

    private final List<K> keys = new ArrayList<>();
    private final List<V> values = new ArrayList<>();
    private final List<BTreeNode<K, V>> children = new ArrayList<>();

    private final int level;

    BTreeNode(int level) {
        this.level = level;
    }

    int getLevel() {
        return level;
    }

    List<K> getKeys() {
        return keys;
    }

    List<V> getValues() {
        return values;
    }

    List<BTreeNode<K, V>> getChildren() {
        return children;
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
        insert(keys, key, index);
        insert(values, value, index);
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

    void addChildNode(int index, BTreeNode<K, V> node) {
        insert(children, node, index);
    }

    BTreeNode<K, V> deleteChildNode(int index) {
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

    static <T> List<T> insert(List<T> list, T element, int insertIndex)  {
        T current = element;
        for(int i = insertIndex; i < list.size(); i++) {
            current = list.set(i, current);
        }

        list.add(current);

        return list;
    }

    @Override
    public String toString() {
        return String.format("node_%s(level=%s) keys=%s", nodeId, level, keys);
    }
}
