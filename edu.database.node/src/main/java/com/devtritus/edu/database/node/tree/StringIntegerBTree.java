package com.devtritus.edu.database.node.tree;

public class StringIntegerBTree extends AbstractBTree<String, Integer> {
    public StringIntegerBTree(int m) {
        super(m);
    }

    @Override
    boolean isFetchedKeySatisfy(String key, String fetchKey) {
        return key.toLowerCase().startsWith(fetchKey.toLowerCase());
    }
}
