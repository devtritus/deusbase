package com.devtritus.edu.database.node.tree;

public interface BTree<K extends Comparable<K>, V> {
    V searchByKey(K key);

    K add(K key, V value);

    boolean delete(K key);

    boolean isEmpty();
}
