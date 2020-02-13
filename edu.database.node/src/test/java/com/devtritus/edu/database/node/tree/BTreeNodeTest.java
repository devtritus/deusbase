package com.devtritus.edu.database.node.tree;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class BTreeNodeTest {

    @Test
    void get_value_test() {
        BTreeNode<String, Long> node = new BTreeNode<>(4);
        node.putKeyValue("c", 1L);
        node.putKeyValue("a", 0L);

        assertThat(node.getValue("a")).isEqualTo(0L);
        assertThat(node.getValue("c")).isEqualTo(1L);

        node.putKeyValue("b", 3L);

        assertThat(node.getValue("b")).isEqualTo(3L);

        node.putKeyValue("d", 4L);

        assertThatThrownBy(() -> node.getValue("a")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> node.getValue("b")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> node.getValue("c")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> node.getValue("d")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void replace_value_test() {
        BTreeNode<String, Long> node = new BTreeNode<>(4);
        node.putKeyValue("c", 1L);
        assertThat(node.getValue("c")).isEqualTo(1L);

        int index = node.searchKey("c");
        node.replaceValue(index, 2L);

        assertThat(node.getValue("c")).isEqualTo(2L);
    }

    @Test
    void delete_key_test() {
        BTreeNode<String, Long> node = new BTreeNode<>(4);
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
        BTreeNode<String, Long> node = new BTreeNode<>(3);
        node.putKeyValue("a", 0L);
        node.putKeyValue("b", 1L);
        node.putKeyValue("c", 2L);

        BTreeNode<String, Long> newNode = node.copy(0, 2);

        assertThat(newNode.getValue("a")).isEqualTo(0L);
        assertThat(newNode.getValue("b")).isEqualTo(1L);

        BTreeNode<String, Long> newNode2 = node.copy(2, 3);

        assertThat(newNode2.getValue("c")).isEqualTo(2L);

        BTreeNode<String, Long> newNode3 = node.copy(0, 1);

        assertThat(newNode3.getValue("a")).isEqualTo(0L);
    }

    @Test
    void get_children_test() {
        BTreeNode<String, Long> node = new BTreeNode<>(3, 1);

        node.putKeyValue("a", 0L);

        BTreeNode<String, Long> childNodeA = new BTreeNode<>(3, 0);

        childNodeA.putKeyValue("b", 1L);

        BTreeNode<String, Long> childNodeB = new BTreeNode<>(3, 0);

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
        BTreeNode<String, Long> node = new BTreeNode<>(3);
        node.putKeyValue("a", 0L);
        node.putKeyValue("b", 1L);

        assertThat(node.getValue("a")).isEqualTo(0L);

        int indexA = node.searchKey("a");
        Entry<String, Long> entryA = node.deleteKeyValue(indexA);

        assertThat(entryA).isEqualTo(new Entry<>("a", 0L));

        assertThat(node.getValue("a")).isNull();

        int indexB = node.searchKey("b");
        assertThatThrownBy(() -> node.deleteKeyValue(indexB)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void union_test() {
        BTreeNode<String, Long> leftNode = new BTreeNode<>(3, 0);

        leftNode.putKeyValue("a", 0L);

        BTreeNode<String, Long> rightNode = new BTreeNode<>(3, 0);

        rightNode.putKeyValue("c", 2L);
        rightNode.putKeyValue("d", 3L);

        BTreeNode<String, Long> union = leftNode.union(rightNode);

        assertThat(union.getKeyValue(0)).isEqualTo(new Entry<>("a", 0L));
        assertThat(union.getKeyValue(1)).isEqualTo(new Entry<>("c", 2L));
        assertThat(union.getKeyValue(2)).isEqualTo(new Entry<>("d", 3L));
    }
}
