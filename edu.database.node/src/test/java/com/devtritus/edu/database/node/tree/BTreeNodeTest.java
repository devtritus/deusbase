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
    void copy_node_test() {
        BTreeNode<String, Long> node = new BTreeNode<>(0);
        node.putKeyValue("a", 0L);
        node.putKeyValue("b", 1L);
        node.putKeyValue("c", 2L);

        BTreeNode<String, Long> newNode = new BTreeNode<>(0);
        newNode.copy(0, 2, node);

        assertThat(newNode.getValue("a")).isEqualTo(0L);
        assertThat(newNode.getValue("b")).isEqualTo(1L);

        BTreeNode<String, Long> newNode2 = new BTreeNode<>(0);
        newNode2.copy(2, 3, node);

        assertThat(newNode2.getValue("c")).isEqualTo(2L);

        BTreeNode<String, Long> newNode3 = new BTreeNode<>(0);
        newNode3.copy(0, 1, node);

        assertThat(newNode3.getValue("a")).isEqualTo(0L);
    }

    @Test
    void get_children_test() {
        BTreeNode<String, Long> node = new BTreeNode<>(0);

        node.putKeyValue("a", 0L);

        BTreeNode<String, Long> childNodeA = new BTreeNode<>(0);

        childNodeA.putKeyValue("b", 1L);

        BTreeNode<String, Long> childNodeB = new BTreeNode<>(0);

        childNodeB.putKeyValue("c", 2L);

        node.addChildNode(0, childNodeA);
        node.addChildNode(1, childNodeB);

        assertThat(node.getChildNode(0)).isEqualTo(childNodeA);
        assertThat(node.getChildNode(1)).isEqualTo(childNodeB);

        assertThat(node.getChildNode(-1)).isEqualTo(null);
        assertThat(node.getChildNode(2)).isEqualTo(null);
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

    @Test
    void union_test() {
        BTreeNode<String, Long> leftNode = new BTreeNode<>(0);

        leftNode.putKeyValue("a", 0L);

        BTreeNode<String, Long> rightNode = new BTreeNode<>(0);

        rightNode.putKeyValue("c", 2L);
        rightNode.putKeyValue("d", 3L);

        leftNode.add(rightNode);

        assertThat(leftNode.getKeyValue(0)).isEqualTo(new Entry<>("a", 0L));
        assertThat(leftNode.getKeyValue(1)).isEqualTo(new Entry<>("c", 2L));
        assertThat(leftNode.getKeyValue(2)).isEqualTo(new Entry<>("d", 3L));
    }


    private <T> List<T> listOf(T... values) {
        return new ArrayList<>(Arrays.asList(values));
    }
}
