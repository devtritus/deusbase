package com.devtritus.edu.database.node;

import com.devtritus.edu.database.core.Api;
import com.devtritus.edu.database.node.tree.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class NodeApi implements Api<String, String> {
    private int valuePosition = 0;

    private BTree<String, Integer> tree;
    private Map<Integer, String> values = new HashMap<>();

    NodeApi() {
        BTreeIndexLoader loader = new BTreeIndexLoader("node.index");
        BTreeNodeDiskProvider provider;
        if(loader.initialized()) {
            provider = loader.load();
        } else {
            provider = loader.initialize(50);
        }

        tree = new StringIntegerBTree(50, provider);
    }

    @Override
    public Map<String, String> create(String key, String value) {
        tree.add(key, ++valuePosition);
        values.put(valuePosition, value);

        return Collections.singletonMap(key, value);
    }

    @Override
    public Map<String, String> read(String key) {
        Map<String, Integer> fetchResult = tree.fetch(key);

        return fetchResult.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> values.get(entry.getValue())));
    }

    @Override
    public Map<String, String> delete(String key) {
        //TODO: don't forget delete value
        tree.delete(key);
        return Collections.singletonMap(key, null);
    }

    @Override
    public Map<String, String> update(String key, String value) {
        tree.add(key, ++valuePosition);
        //TODO: don't forget delete old value
        values.put(valuePosition, value);
        return Collections.singletonMap(key, value);
    }
}
