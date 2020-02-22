package com.devtritus.edu.database.node.tree;

abstract class StringKeyAbstractBTree<V> extends AbstractBTree<String, V> {
    StringKeyAbstractBTree(int m) {
        super(m);
    }

    @Override
    boolean isFetchedKeySatisfy(String key, String fetchKey) {
        return key.toLowerCase().startsWith(fetchKey.toLowerCase());
    }
}
