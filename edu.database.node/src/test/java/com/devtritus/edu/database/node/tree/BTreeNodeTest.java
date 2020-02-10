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

        node.putKeyValue("c", 2L);

        assertThat(node.getValue("c")).isEqualTo(2L);

        node.putKeyValue("b", 3L);

        assertThat(node.getValue("b")).isEqualTo(3L);

        node.putKeyValue("d", 4L);

        assertThatThrownBy(() -> node.getValue("a")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> node.getValue("b")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> node.getValue("c")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> node.getValue("d")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void delete_key_test() {
        BTreeNode<String, Long> node = new BTreeNode<>(4);
        node.putKeyValue("a", 0L);
        node.putKeyValue("b", 1L);
        node.putKeyValue("c", 2L);

        node.deleteKey("b");

        assertThat(node.getValue("a")).isEqualTo(0L);
        assertThat(node.getValue("c")).isEqualTo(2L);
        assertThat(node.getValue("b")).isNull();

        node.deleteKey("a");

        assertThat(node.getValue("c")).isEqualTo(2L);
        assertThat(node.getValue("a")).isNull();
        assertThat(node.getValue("b")).isNull();

        node.deleteKey("c");

        assertThat(node.getValue("a")).isNull();
        assertThat(node.getValue("b")).isNull();
        assertThat(node.getValue("c")).isNull();
    }
}
