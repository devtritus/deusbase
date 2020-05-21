package com.devtritus.deusbase.api;

import java.util.*;

public class ProgramArgs {
    final Map<String, String> data;

    ProgramArgs(Map<String, String> data) {
        this.data = data;
    }

    public boolean contains(String key) {
        return data.containsKey(key);
    }

    public String getOrDefault(String key, String defaultValue) {
        String value = data.get(key);
        return value != null ? value : defaultValue;
    }

    public Integer getIntegerOrDefault(String key, Integer defaultValue) {
        String value = data.get(key);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }

    public String get(String key) {
        return getOrThrow(key);
    }

    public int getInteger(String key) {
        String value = getOrThrow(key);
        return Integer.parseInt(value);
    }

    public List<String> getList(String key) {
        String value = getOrThrow(key);
        String[] values = value.split("[,;:]");
        return Arrays.asList(values);
    }

    private String getOrThrow(String key) {
        String value = data.get(key);
        if(value == null) {
            throw new IllegalArgumentException(String.format("Value by key %s was not found", key));
        }
        return value;
    }
}
