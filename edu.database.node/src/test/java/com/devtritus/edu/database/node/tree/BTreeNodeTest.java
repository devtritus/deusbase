package com.devtritus.edu.database.node.tree;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class BTreeNodeTest {
    @Test
    void get_value_test() {
        BTreeNode node = new BTreeNode(0, 0);
        node.putKeyValue("c", 1);
        node.putKeyValue("a", 0);

        assertThat(node.getValue("a")).isEqualTo(0L);
        assertThat(node.getValue("c")).isEqualTo(1L);

        node.putKeyValue("b", 3);

        assertThat(node.getValue("b")).isEqualTo(3L);
    }

    @Test
    void delete_key_test() {
        BTreeNode node = new BTreeNode(0, 0);
        node.putKeyValue("a", 0);
        node.putKeyValue("b", 1);
        node.putKeyValue("c", 2);

        int index = node.searchKey("b");
        node.deleteKeyValue(index);

        assertThat(node.getValue("a")).isEqualTo(0L);
        assertThat(node.getValue("c")).isEqualTo(2L);
        assertThat(node.getValue("b")).isNull();

        index = node.searchKey("a");
        node.deleteKeyValue(index);

        assertThat(node.getValue("c")).isEqualTo(2L);
        assertThat(node.getValue("a")).isNull();
        assertThat(node.getValue("b")).isNull();

        index = node.searchKey("c");
        node.deleteKeyValue(index);

        assertThat(node.getValue("a")).isNull();
        assertThat(node.getValue("b")).isNull();
        assertThat(node.getValue("c")).isNull();
    }

    @Test
    void delete_children_test() {
        BTreeNode node = new BTreeNode(0, 0);
        node.putKeyValue("a", 0);

        assertThat(node.getValue("a")).isEqualTo(0L);

        int indexA = node.searchKey("a");
        Entry<String, Integer> entryA = node.deleteKeyValue(indexA);

        assertThat(entryA).isEqualTo(new Entry<>("a", 0L));

        assertThat(node.getValue("a")).isNull();
    }
}
