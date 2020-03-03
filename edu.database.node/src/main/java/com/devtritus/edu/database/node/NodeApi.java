package com.devtritus.edu.database.node;

import com.devtritus.edu.database.core.Api;
import com.devtritus.edu.database.node.storage.ValueDiskStorage;
import com.devtritus.edu.database.node.storage.ValueStorage;
import com.devtritus.edu.database.node.tree.*;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class NodeApi implements Api<String, String> {
    private BTree<String, Long> tree;
    private ValueStorage valueStorage;

    NodeApi() {
        BTreeIndexLoader loader = new BTreeIndexLoader("node.index");
        BTreeNodeDiskProvider provider;
        if(loader.initialized()) {
            provider = loader.load();
        } else {
            provider = loader.initialize(50);
        }

        tree = new StringLongBTree(50, provider);
        valueStorage = new ValueDiskStorage(Paths.get("value.storage"));
    }

    @Override
    public Map<String, String> create(String key, String value) {
        long valuePosition = valueStorage.put(value);
        tree.add(key, valuePosition);

        return Collections.singletonMap(key, value);
    }

    @Override
    public Map<String, String> read(String key) {
        Map<String, Long> fetchResult = tree.fetch(key);

        Map<Long, String> values = valueStorage.get(new ArrayList<>(fetchResult.values()));

        return fetchResult.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> values.get(entry.getValue())));
    }

    @Override
    public Map<String, String> delete(String key) {
        tree.delete(key);
        return Collections.singletonMap(key, null);
    }

    @Override
    public Map<String, String> update(String key, String value) {
        long valuePosition = valueStorage.put(value);
        tree.add(key, valuePosition);
        return Collections.singletonMap(key, value);
    }
}
