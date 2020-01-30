package com.devtritus.edu.database.node.tree;

import java.util.List;

public interface BTree<K, V> {
    K add(K key, V value);
    K delete(K key);
    List<V> search(K key);
}
