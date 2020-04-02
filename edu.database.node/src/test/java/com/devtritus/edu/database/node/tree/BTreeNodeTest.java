package com.devtritus.edu.database.node.tree;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.devtritus.edu.database.node.TestUtils.*;
import static org.assertj.core.api.Assertions.*;

class BTreeNodeTest {
    @Test
    void get_value_test() {
        BTreeNode node = new BTreeNode(0, 0);
        node.putKeyValue("c", listOf(1L));
        node.putKeyValue("a", listOf(0L));

        assertThat(node.getValue("a")).containsOnly(0L);
        assertThat(node.getValue("c")).containsOnly(1L);

        node.putKeyValue("b", listOf(3L));

        assertThat(node.getValue("b")).containsOnly(3L);
    }

    @Test
    void delete_key_test() {
        BTreeNode node = new BTreeNode(0, 0);
        node.putKeyValue("a", listOf(0L));
        node.putKeyValue("b", listOf(1L));
        node.putKeyValue("c", listOf(2L));

        int index = node.searchKey("b");
        node.deleteKeyValue(index);

        assertThat(node.getValue("a")).containsOnly(0L);
        assertThat(node.getValue("c")).containsOnly(2L);
        assertThat(node.getValue("b")).isNull();

        index = node.searchKey("a");
        node.deleteKeyValue(index);

        assertThat(node.getValue("c")).containsOnly(2L);
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
        node.putKeyValue("a", listOf(0L));

        assertThat(node.getValue("a")).isEqualTo(listOf(0L));

        int indexA = node.searchKey("a");
        Pair<String, List<Long>> entryA = node.deleteKeyValue(indexA);

        assertThat(entryA.first).isEqualTo("a");
        assertThat(entryA.second).containsOnly(0L);

        assertThat(node.getValue("a")).isNull();
    }
}
