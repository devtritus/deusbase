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

    final int m;
    final int max;
    final int min;
    final int level;

    BTreeNode(int m) {
        this(m, 0);
    }

    BTreeNode(int m, int level) {
        if(m < 3) {
            throw new IllegalArgumentException("m must be more or equal then 3");
        }
        this.m = m;
        this.max = m - 1;
        //m = 3 => 3/2 = 1.5 ~ 2 => t = 2 => t - 1 = 2 - 1
        //m = 4 => 4/2 =   2 ~ 2 => t = 2 => t - 1 = 2 - 1
        this.min = (int)Math.ceil(m / 2d) - 1;
        this.level = level;
    }

    void putKeyValue(K key, V value) {
        int index = searchKey(key);

        if(index > -1) {
            throw new IllegalStateException(String.format("Key %s already exists", key));
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

        int keySize = getKeysSize();

        Entry<K, V> keyValue = getKeyValue(index);

        keys.remove(index);
        values.remove(index);

        return keyValue;
    }

    void replaceKeyValue(int index, K key, V value) {
        keys.set(index, key);
        values.set(index, value);
    }

    void replaceValue(int index, V value) {
        if(index < 0) {
            throw new IllegalStateException(String.format("Negative index %s", index));
        }

        values.set(index, value);
    }

    V getValue(K key) {
        int size = getKeysSize();
        if(max < size) {
            throw new IllegalStateException(String.format("Node has %s keys. Reading permitted only if keys number is %s", size, max));
        }

        int index = searchKey(key);
        if(index < 0) {
            return null;
        } else {
            return values.get(index);
        }
    }

    int searchKey(K key) {
        return Collections.binarySearch(keys, key);
    }

    BTreeNode<K, V> copy(int start, int end) {
        BTreeNode<K, V> newNode = new BTreeNode<>(m, level);

        newNode.keys.addAll(new ArrayList<>(keys.subList(start, end)));
        newNode.values.addAll(new ArrayList<>(values.subList(start, end)));

        if(!children.isEmpty()) {
            newNode.children.addAll(new ArrayList<>(children.subList(start, end + 1)));
        }

        return newNode;
    }

    BTreeNode<K, V> union(BTreeNode<K, V> anotherNode) {
        if (level != anotherNode.level) {
            throw new IllegalStateException(String.format("Nodes %s and %s from different levels", this, anotherNode));
        }

        BTreeNode<K, V> unionNode = new BTreeNode<>(m, level);

        unionNode.keys.addAll(keys);
        unionNode.keys.addAll(anotherNode.keys);

        unionNode.values.addAll(values);
        unionNode.values.addAll(anotherNode.values);

        unionNode.children.addAll(children);
        unionNode.children.addAll(anotherNode.children);

        return unionNode;
    }

    void addChildNode(int index, BTreeNode<K, V> node) {
        insert(children, node, index);
    }

    int deleteChild(BTreeNode<K, V> node) {
        int index = children.indexOf(node);
        if(index == -1) {
            throw new IllegalStateException(String.format("Node %s doesn't contain a child node %s", this, node));
        }
        if(node.getKeysSize() < min - 1) {
            throw new IllegalStateException(String.format("Number of keys less then t - 1. t = %s, keys size = %s", min, keys));
        }
        children.remove(index);
        return index;
    }

    Entry<K, V> getKeyValue(int index) {
        K key = keys.get(index);
        V value = values.get(index);
        return new Entry<>(key, value);
    }

    boolean isLeaf() {
        return level == 0;
    }

    BTreeNode<K, V> getChildNode(int index) {
        int childrenSize = children.size();

        int keysSize = getKeysSize();
        if(keysSize >= childrenSize) {
            throw new IllegalStateException(String.format("Number of children must be as keys number + 1. m - %s, number of keys - %s", m, keysSize));
        }

        if(index < 0 || index >= childrenSize) {
            return null;
        }

        return children.get(index);
    }

    int indexOfChildNode(BTreeNode<K, V> node) {
        return children.indexOf(node);
    }

    List<K> getKeys() {
        return new ArrayList<>(keys);
    }

    List<BTreeNode<K, V>> getChildren() {
        return new ArrayList<>(children);
    }

    int getKeysSize() {
        return keys.size();
    }

    int getChildrenSize() {
        return children.size();
    }

    private static <T> List<T> insert(List<T> list, T element, int insertIndex)  {
        T current = element;
        for(int i = insertIndex; i < list.size(); i++) {
            current = list.set(i, current);
        }

        list.add(current);

        return list;
    }

    private BTreeNode<K, V> copy(int start, int end, BTreeNode<K, V> node) {
        BTreeNode<K, V> newNode = new BTreeNode<>(node.m, node.level);

        newNode.keys.addAll(new ArrayList<>(node.keys.subList(start, end)));
        newNode.values.addAll(new ArrayList<>(node.values.subList(start, end)));

        if(!node.children.isEmpty()) {
            newNode.children.addAll(new ArrayList<>(children.subList(start, end + 1)));
        }

        return newNode;
    }

    @Override
    public String toString() {
        return String.format("node_%s(level=%s, min=%s, max=%s, m=%s) keys=%s", nodeId, level, min, max, m, keys);
    }
}
