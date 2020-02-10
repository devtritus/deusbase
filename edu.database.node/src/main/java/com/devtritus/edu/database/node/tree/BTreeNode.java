package com.devtritus.edu.database.node.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class BTreeNode<K extends Comparable<K>, V> {
    private final List<K> keys = new ArrayList<>();
    private final List<V> values = new ArrayList<>();
    private final List<BTreeNode<K, V>> children = new ArrayList<>();

    private final int m;
    private final int t;

    BTreeNode(int m) {
        if(m < 3) {
            throw new IllegalArgumentException("m must be more or equal then 3");
        }
        this.m = m;
        this.t = m / 2;
    }

    void putKeyValue(K key, V value) {
        int index = searchKey(key);
        if(index > -1) {
            values.set(index, value);
        } else {
            int insertionIndex = -index - 1;
            insert(keys, key, insertionIndex);
            insert(values, value, insertionIndex);
        }
    }

    V getValue(K key) {
        if(m <= keys.size()) {
            throw new IllegalStateException(String.format("Node has %s keys. Reading permitted only if keys number is %s", keys.size(), m - 1));
        }

        int index = searchKey(key);
        if(index < 0) {
            return null;
        } else {
            return values.get(index);
        }
    }

    void deleteKey(K key) {
        int index = searchKey(key);
        if(index < 0) {
            throw new IllegalStateException("Key not found");
        }
        keys.remove(index);
        values.remove(index);
    }

    int searchKey(K key) {
        return Collections.binarySearch(keys, key);
    }

    BTreeNode getLeftChild(BTreeNode currentChild) {
        return null;
    }

    BTreeNode getRightChild(BTreeNode currentChild) {
        return null;
    }

    BTreeNode getChildNode(int index) {
        if(keys.size() - 1 != children.size()) {
            throw new IllegalStateException();
        }
        return children.get(index);
    }

    static <T> List<T> insert(List<T> list, T element, int insertIndex)  {
        T current = element;
        for(int i = insertIndex; i < list.size(); i++) {
            current = list.set(i, current);
        }

        list.add(current);

        return list;
    }

    static class Pair<K, V> {
        final K key;
        final V value;

        Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}
