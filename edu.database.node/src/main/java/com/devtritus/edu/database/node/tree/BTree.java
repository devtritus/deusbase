package com.devtritus.edu.database.node.tree;

import java.util.List;

public interface BTree<K extends Comparable<K>, V> {
    List<V> search(K key);

    K add(K key, V value);

    K delete(K key);

    boolean isEmpty();
}
