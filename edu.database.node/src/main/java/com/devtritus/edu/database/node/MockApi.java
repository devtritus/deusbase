package com.devtritus.edu.database.node;

import com.devtritus.edu.database.core.Api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MockApi implements Api<String, String> {
    private final Map<String, String> data = new HashMap<>();

    @Override
    public Map<String, String> create(String key, String value) {
        data.put(key, value);

        return Collections.singletonMap(key, value);
    }

    @Override
    public Map<String, String> read(String key) {
        String value = data.get(key);

        return Collections.singletonMap(key, value);
    }

    @Override
    public Map<String, String> delete(String key) {
        String value = data.remove(key);

        return Collections.singletonMap(key, value);
    }

    @Override
    public Map<String, String> update(String key, String value) {
        String oldValue = data.put(key, value);

        return Collections.singletonMap(key, oldValue);
    }
}
