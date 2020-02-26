package com.devtritus.edu.database.node;

import com.devtritus.edu.database.core.Api;
import com.devtritus.edu.database.node.tree.BTree;
import com.devtritus.edu.database.node.tree.StringIntegerBTree;

import java.util.Collections;
import java.util.Map;

public class NodeApi implements Api<String, String> {
    private BTree<String, Integer> tree = new StringIntegerBTree(50);

    @Override
    public Map<String, String> create(String key, String value) {
        tree.add(key, 0);
        return Collections.singletonMap(key, value);
    }

    @Override
    public Map<String, String> read(String key) {
        tree.fetch(key);
        return null;
    }

    @Override
    public Map<String, String> delete(String key) {
        tree.delete(key);
        return Collections.singletonMap(key, null);
    }

    @Override
    public Map<String, String> update(String key, String value) {
        tree.add(key, 0);
        return Collections.singletonMap(key, value);
    }
}
