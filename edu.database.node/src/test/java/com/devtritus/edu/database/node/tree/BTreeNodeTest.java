package com.devtritus.edu.database.node.tree;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class BTreeNodeTest {
    @Test
    void insert_element_to_list_test() {
        assertThat(BTreeNode.insert(listOf(), 1, 0)).containsExactly(1);
        assertThat(BTreeNode.insert(listOf(2), 1, 0)).containsExactly(1, 2);
        assertThat(BTreeNode.insert(listOf(1), 2, 1)).containsExactly(1, 2);
        assertThat(BTreeNode.insert(listOf(1, 3, 4), 2, 1)).containsExactly(1, 2, 3, 4);
        assertThat(BTreeNode.insert(listOf(1, 2, 3), 4, 3)).containsExactly(1, 2, 3, 4);
    }

    @Test
    void get_value_test() {
        BTreeNode<String, Long> node = new BTreeNode<>(0);
        node.putKeyValue("c", 1L);
        node.putKeyValue("a", 0L);

        assertThat(node.getValue("a")).isEqualTo(0L);
        assertThat(node.getValue("c")).isEqualTo(1L);

        node.putKeyValue("b", 3L);

        assertThat(node.getValue("b")).isEqualTo(3L);
    }

    @Test
    void delete_key_test() {
        BTreeNode<String, Long> node = new BTreeNode<>(0);
        node.putKeyValue("a", 0L);
        node.putKeyValue("b", 1L);
        node.putKeyValue("c", 2L);

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
        BTreeNode<String, Long> node = new BTreeNode<>(0);
        node.putKeyValue("a", 0L);

        assertThat(node.getValue("a")).isEqualTo(0L);

        int indexA = node.searchKey("a");
        Entry<String, Long> entryA = node.deleteKeyValue(indexA);

        assertThat(entryA).isEqualTo(new Entry<>("a", 0L));

        assertThat(node.getValue("a")).isNull();
    }

    private <T> List<T> listOf(T... values) {
        return new ArrayList<>(Arrays.asList(values));
    }
}
