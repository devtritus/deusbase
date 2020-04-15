package com.devtritus.deusbase.node.role;

import com.devtritus.deusbase.api.Api;
import java.util.List;
import java.util.Map;

public class SlaveApiDecorator<K, V> implements Api<K, V> {
    private Api<K, V> api;
    private SlaveNode slaveNode;

    public SlaveApiDecorator(Api<K, V> api, SlaveNode slaveNode) {
        this.api = api;
        this.slaveNode = slaveNode;
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
        throwSlaveException();
        return null;
    }

    @Override
    public Map<K, List<V>> update(K key, int valueIndex, V value) {
        throwSlaveException();
        return null;
    }

    @Override
    public Map<K, List<V>> delete(K key, int valueIndex) {
        throwSlaveException();
        return null;
    }

    private void throwSlaveException() {
        throw new IllegalStateException("Node has been running as SLAVE. Write operations are forbidden");
    }
}
