package com.devtritus.edu.database.node.tree;

import org.junit.jupiter.api.Test;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class BTreeNodeBytesConverterTest {

    @Test
    void serialize_deserialize_leaf_node_test() throws IOException {
        BTreeNode node = new BTreeNode(42, 3);
        node.putKeyValue("Jack Nicholson", 3000000L);
        node.putKeyValue("Sean Connery", 2000000L);

        byte[] bytes = BTreeNodeBytesConverter.toBytes(node);
        BTreeNode result = BTreeNodeBytesConverter.fromBytes(bytes);

        assertThat(result.getChildren()).isEmpty();
    }

    @Test
    void serialize_deserialize_empty_node_test() throws IOException {
        BTreeNode node = new BTreeNode(42, 0);

        byte[] bytes = BTreeNodeBytesConverter.toBytes(node);
        BTreeNode result = BTreeNodeBytesConverter.fromBytes(bytes);

        assertThat(result.getNodeId()).isEqualTo(42);
        assertThat(result.getLevel()).isEqualTo(0);
        assertThat(result.getKeys()).isEmpty();
        assertThat(result.getChildren()).isEmpty();
    }

    @Test
    void serialize_deserialize_inner_node_test() throws IOException {
        BTreeNode node = new BTreeNode(42, 3);
        node.putKeyValue("Jack Nicholson", 4000000L);
        node.putKeyValue("Sean Connery", 2000000L);
        node.putKeyValue("Tom Hanks", 1000000L);
        node.putKeyValue("José Luis Alcaine", 3000000L);

        node.insertChildNode(0, 1993);
        node.insertChildNode(1, 1994);
        node.insertChildNode(2, 1995);
        node.insertChildNode(3, 1996);
        node.insertChildNode(4, 1997);

        byte[] bytes = BTreeNodeBytesConverter.toBytes(node);
        BTreeNode result = BTreeNodeBytesConverter.fromBytes(bytes);

        assertThat(result.getNodeId()).isEqualTo(42);
        assertThat(result.getLevel()).isEqualTo(3);
        assertThat(result.getKeyValue(0)).isEqualTo(new Entry<>("Jack Nicholson", 4000000L));
        assertThat(result.getKeyValue(1)).isEqualTo(new Entry<>("José Luis Alcaine", 3000000L));
        assertThat(result.getKeyValue(2)).isEqualTo(new Entry<>("Sean Connery", 2000000L));
        assertThat(result.getKeyValue(3)).isEqualTo(new Entry<>("Tom Hanks", 1000000L));
        assertThat(result.getChildren()).containsExactly(1993, 1994, 1995, 1996, 1997);
    }
}
