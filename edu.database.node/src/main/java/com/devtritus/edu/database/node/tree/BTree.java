package com.devtritus.edu.database.node.tree;

import java.util.Map;

public interface BTree<K extends Comparable<K>, V> {
    Map<K, V> fetch(K key);

    V searchByKey(K key);

    K add(K key, V value);

    boolean delete(K key);

    boolean isEmpty();
}
