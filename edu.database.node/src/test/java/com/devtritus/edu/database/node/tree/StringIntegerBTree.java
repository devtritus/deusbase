package com.devtritus.edu.database.node.tree;

class StringIntegerBTree extends AbstractBTree<String, Integer> {
    StringIntegerBTree(int m) {
        super(m);
    }

    @Override
    boolean isFetchedKeySatisfy(String key, String fetchKey) {
        return key.startsWith(fetchKey);
    }
}
