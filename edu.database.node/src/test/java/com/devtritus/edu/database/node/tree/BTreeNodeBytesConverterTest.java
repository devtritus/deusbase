package com.devtritus.edu.database.node.tree;

import org.junit.jupiter.api.Test;
import java.io.IOException;

import static com.devtritus.edu.database.node.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

class BTreeNodeBytesConverterTest {

    @Test
    void serialize_deserialize_leaf_node_test() throws IOException {
        BTreeNode node = new BTreeNode(42, 3);
        node.putKeyValue("Jack Nicholson", listOf(3000000L));
        node.putKeyValue("Sean Connery", listOf(2000000L));

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
        node.putKeyValue("Jack Nicholson", listOf(4000000L));
        node.putKeyValue("Sean Connery", listOf(2000000L));
        node.putKeyValue("Tom Hanks", listOf(1000000L));
        node.putKeyValue("José Luis Alcaine", listOf(3000000L));

        node.insertChildNode(0, 1993);
        node.insertChildNode(1, 1994);
        node.insertChildNode(2, 1995);
        node.insertChildNode(3, 1996);
        node.insertChildNode(4, 1997);

        byte[] bytes = BTreeNodeBytesConverter.toBytes(node);
        BTreeNode result = BTreeNodeBytesConverter.fromBytes(bytes);

        assertThat(result.getNodeId()).isEqualTo(42);
        assertThat(result.getLevel()).isEqualTo(3);
        assertThat(result.getKeyValue(0)).isEqualTo(new Entry<>("Jack Nicholson", listOf(4000000L)));
        assertThat(result.getKeyValue(1)).isEqualTo(new Entry<>("José Luis Alcaine", listOf(3000000L)));
        assertThat(result.getKeyValue(2)).isEqualTo(new Entry<>("Sean Connery", listOf(2000000L)));
        assertThat(result.getKeyValue(3)).isEqualTo(new Entry<>("Tom Hanks", listOf(1000000L)));
        assertThat(result.getChildren()).containsExactly(1993, 1994, 1995, 1996, 1997);
    }

    @Test
    void serialize_deserialize_leaf_node_with_multiple_values_test() throws IOException {
        BTreeNode node = new BTreeNode(42, 3);
        node.putKeyValue("Gary Oldman", listOf(1000000L));
        node.putKeyValue("Leonardo DiCaprio", listOf(2000000L, 3000000L, 4000000L));
        node.putKeyValue("Robert De Niro ", listOf(6000000L, 7000000L));
        node.putKeyValue("Tom Hanks", listOf(5000000L));

        byte[] bytes = BTreeNodeBytesConverter.toBytes(node);
        BTreeNode result = BTreeNodeBytesConverter.fromBytes(bytes);

        assertThat(result.getNodeId()).isEqualTo(42);
        assertThat(result.getLevel()).isEqualTo(3);
        assertThat(result.getKeyValue(0)).isEqualTo(new Entry<>("Gary Oldman", listOf(1000000L)));
        assertThat(result.getKeyValue(1)).isEqualTo(new Entry<>("Leonardo DiCaprio", listOf(2000000L, 3000000L, 4000000L)));
        assertThat(result.getKeyValue(2)).isEqualTo(new Entry<>("Robert De Niro ", listOf(6000000L, 7000000L)));
        assertThat(result.getKeyValue(3)).isEqualTo(new Entry<>("Tom Hanks", listOf(5000000L)));
        assertThat(result.getChildren()).isEmpty();
    }
}
