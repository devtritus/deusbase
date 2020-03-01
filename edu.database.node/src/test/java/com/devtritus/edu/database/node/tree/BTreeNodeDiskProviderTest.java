package com.devtritus.edu.database.node.tree;

import org.junit.jupiter.api.Test;

 class BTreeNodeDiskProviderTest {
    @Test
    void test() {
        BTreeIndexLoader loader = new BTreeIndexLoader("btree.index");
        if(loader.initialized()) {
            loader.load();
        } else {
            loader.initialize(50);
        }
    }
}
