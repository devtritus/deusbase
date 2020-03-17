package com.devtritus.edu.database.node;

import com.devtritus.edu.database.core.Api;

import java.util.*;

public class MockApi implements Api<String, String> {
    private final Map<String, List<String>> data = new HashMap<>();

    @Override
    public Map<String, List<String>> read(String key) {
        return Collections.singletonMap(key, data.get(key));
    }

    @Override
    public Map<String, List<String>> search(String key) {
        return read(key);
    }

    @Override
    public Map<String, List<String>> create(String key, String value) {
        List<String> values = data.get(key);

        if(values != null) {
            values.add(value);
        } else {
            data.put(key, new ArrayList<>(Collections.singletonList(value)));
        }

        return read(key);
    }

    @Override
    public Map<String, List<String>> update(String key, int valueIndex, String value) {
        List<String> values = data.get(key);

        if(values != null) {
            values.set(valueIndex, value);
        } else {
            create(key, value);
        }

        return read(key);
    }

    @Override
    public Map<String, List<String>> delete(String key, int valueIndex) {
        List<String> values = data.get(key);

        if(values != null) {
            if(values.size() == 1) {
                data.remove(key);
            } else {
                values.remove(valueIndex);
            }
        }

        return read(key);
    }
}
