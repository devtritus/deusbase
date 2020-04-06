package com.devtritus.deusbase.node.utils;

import com.devtritus.deusbase.core.Api;

import java.util.List;
import java.util.Map;

public class LoggingApiDecorator<K, V> implements Api<K, V> {
    private Api<K, V> api;

    public LoggingApiDecorator(Api<K, V> api) {
        this.api = api;
    }

    @Override
    public Map<K, List<V>> read(K key) {
        log("read", key);
        Map<K, List<V>> result = api.read(key);
        logSuccess(result);
        return result;
    }

    @Override
    public Map<K, List<V>> search(K key) {
        log("search", key);
        Map<K, List<V>> result = api.search(key);
        logSuccess(result);
        return result;
    }

    @Override
    public Map<K, List<V>> create(K key, V value) {
        log("create", key, value);
        Map<K, List<V>> result = api.create(key, value);
        logSuccess(result);
        return result;
    }

    @Override
    public Map<K, List<V>> update(K key, int valueIndex, V value) {
        log("update", key, value);
        Map<K, List<V>> result = api.update(key, valueIndex, value);
        logSuccess(result);
        return result;
    }

    @Override
    public Map<K, List<V>> delete(K key, int valueIndex) {
        log("delete", key);
        Map<K, List<V>> result = api.delete(key, valueIndex);
        logSuccess(result);
        return result;
    }

    private static void log(String command, Object... params) {
        System.out.format("\nExecute command \"%s\"\nargs:\n", command);
        for(Object param : params) {
            System.out.println(param);
        }
    }

    private static void logSuccess(Object result) {
        System.out.format("\nresult: %s\n", result);
        System.out.println("\nSuccess\n");
    }
}
