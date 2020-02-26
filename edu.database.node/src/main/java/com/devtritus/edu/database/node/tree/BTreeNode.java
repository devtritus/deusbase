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

    boolean isLeaf() {
        return level == 0;
    }

    int getKeysSize() {
        return keys.size();
    }

    int getChildrenSize() {
        return children.size();
    }

    int searchKey(K key) {
        return Collections.binarySearch(keys, key);
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

    Entry<K, V> getKeyValue(int index) {
        K key = keys.get(index);
        V value = values.get(index);
        return new Entry<>(key, value);
    }

    void copy(int start, int end, BTreeNode<K, V> fromNode) {
        keys.addAll(fromNode.getKeys().subList(start, end));
        values.addAll(fromNode.getValues().subList(start, end));

        if(!fromNode.getChildren().isEmpty()) {
            children.addAll(fromNode.getChildren().subList(start, end + 1));
        }
    }

    void add(BTreeNode<K, V> anotherNode) {
        if (level != anotherNode.level) {
            throw new IllegalStateException(String.format("Nodes %s and %s from different levels", this, anotherNode));
        }

        keys.addAll(anotherNode.keys);
        values.addAll(anotherNode.values);
        children.addAll(anotherNode.children);
    }

    void addChildNode(int index, BTreeNode<K, V> node) {
        insert(children, node, index);
    }

    BTreeNode<K, V> getChildNode(int index) {
        int childrenSize = children.size();

        int keysSize = getKeysSize();
        if(keysSize >= childrenSize) {
            throw new IllegalStateException(String.format("Number of children must be equal - %s but number of children - %s, number of keys - %s", keysSize + 1, childrenSize, keysSize));
        }

        if(index < 0 || index >= childrenSize) {
            return null;
        }

        return children.get(index);
    }

    void deleteInterval(int fromIndex, int toIndex) {
        keys.subList(fromIndex, toIndex).clear();
        values.subList(fromIndex, toIndex).clear();
        if(!children.isEmpty()) {
            children.subList(fromIndex, toIndex + 1).clear();
        }
    }

    int indexOfChild(BTreeNode<K, V> child) {
        int index = children.indexOf(child);
        if(index == -1) {
            throw new IllegalStateException(String.format("Node %s doesn't contain a child node %s", this, child));
        }

        return index;
    }

    List<K> getKeys() {
        return new ArrayList<>(keys);
    }

    BTreeNode<K, V> deleteChild(int index) {
        return children.remove(index);
    }

    List<V> getValues() {
        return new ArrayList<>(values);
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

    List<BTreeNode<K, V>> getChildren() {
        return new ArrayList<>(children);
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
