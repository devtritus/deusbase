package com.devtritus.edu.database.core;

import java.util.Map;

public interface CRUDApi<K, V> {
    Map<K, V> create(K key, V value);
    Map<K, V> read(K key);
    Map<K, V> delete(K key);
    Map<K, V> update(K key, V value);
}
