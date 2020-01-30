package com.devtritus.edu.database.node.tree;

import org.junit.jupiter.api.Test;

class StringBTreeTest {
    @Test
    void add_test() {
        StringBTree tree = new StringBTree(3);
        tree.add("aaaa", 4L);
        tree.add("aaaaa", 5L);
        tree.add("aaaaaa", 6L);
        tree.add("aaaaaaa", 7L);
        tree.add("a", 1L);
        tree.add("aa", 2L);
        tree.add("aaa", 3L);
        tree.add("aaaaaaaa", 8L);
        tree.add("aaaaaaaaa", 9L);
        tree.add("aaaaaaaaaa", 10L);
        System.out.println(tree);
    }

    @Test
    void delete_test() {

    }

    @Test
    void search_test() {
        StringBTree tree = new StringBTree(3);
    }
}
