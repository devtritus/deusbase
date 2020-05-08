package com.devtritus.deusbase.api;

import java.util.List;
import java.util.Map;

public interface Api<K, V> {
    Map<K, List<V>> read(K key);
    Map<K, List<V>> search(K key);

    Map<K, List<V>> create(K key, V value);
    Map<K, List<V>> update(K key, int valueIndex, V value);
    Map<K, List<V>> delete(K key);
    Map<K, List<V>> delete(K key, int valueIndex);
}
