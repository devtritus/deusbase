package com.devtritus.deusbase.node.tree;

import java.util.Map;

public interface BTree<K extends Comparable<K>, V> {
    Map<K, V> fetch(K key);

    V searchByKey(K key);

    K add(K key, V value);

    boolean deleteKey(K key);

    boolean isEmpty();
}
