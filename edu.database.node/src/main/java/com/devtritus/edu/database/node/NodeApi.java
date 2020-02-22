package com.devtritus.edu.database.node;

import com.devtritus.edu.database.core.Api;
import com.devtritus.edu.database.node.tree.BTree;
import com.devtritus.edu.database.node.tree.BTreeImpl;
import java.util.Collections;
import java.util.Map;

public class NodeApi implements Api<String, String> {
    private BTree<String, String> tree = new BTreeImpl(50);

    @Override
    public Map<String, String> create(String key, String value) {
        tree.add(key, value);
        return Collections.singletonMap(key, value);
    }

    @Override
    public Map<String, String> read(String key) {
        return tree.fetch(key);
    }

    @Override
    public Map<String, String> delete(String key) {
        tree.delete(key);
        return Collections.singletonMap(key, null);
    }

    @Override
    public Map<String, String> update(String key, String value) {
        tree.add(key, value);
        return Collections.singletonMap(key, value);
    }
}
