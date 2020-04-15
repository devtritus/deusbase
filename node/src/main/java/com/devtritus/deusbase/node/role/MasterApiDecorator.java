package com.devtritus.deusbase.node.role;

import com.devtritus.deusbase.api.Api;
import java.util.List;
import java.util.Map;

public class MasterApiDecorator<K, V> implements Api<K, V> {
    private Api<K, V> api;
    private MasterNode masterNode;

    public MasterApiDecorator(Api<K, V> api, MasterNode masterNode) {
        this.api = api;
        this.masterNode = masterNode;
    }

    @Override
    public Map<K, List<V>> read(K key) {
        Map<K, List<V>> result = api.read(key);
        return result;
    }

    @Override
    public Map<K, List<V>> search(K key) {
        Map<K, List<V>> result = api.search(key);
        return result;
    }

    @Override
    public Map<K, List<V>> create(K key, V value) {
        Map<K, List<V>> result = api.create(key, value);
        return result;
    }

    @Override
    public Map<K, List<V>> update(K key, int valueIndex, V value) {
        Map<K, List<V>> result = api.update(key, valueIndex, value);
        return result;
    }

    @Override
    public Map<K, List<V>> delete(K key, int valueIndex) {
        Map<K, List<V>> result = api.delete(key, valueIndex);
        return result;
    }
}
