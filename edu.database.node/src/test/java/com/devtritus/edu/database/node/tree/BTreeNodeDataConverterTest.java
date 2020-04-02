package com.devtritus.edu.database.node.tree;

import org.junit.jupiter.api.Test;
import java.io.IOException;

import static com.devtritus.edu.database.node.TestUtils.listOf;
import static org.assertj.core.api.Assertions.assertThat;

class BTreeNodeDataConverterTest {

    @Test
    void serialize_deserialize_leaf_data_test() throws IOException {
        BTreeNodeData data = new BTreeNodeData();
        data.setNodeId(42);
        data.setLevel(3);

        data.setKeys(listOf("Jack Nicholson", "Sean Connery"));
        data.setValues(listOf(listOf(3000000L), listOf(2000000L)));

        byte[] bytes = BTreeNodeDataConverter.toBytes(data);
        BTreeNodeData result = BTreeNodeDataConverter.fromBytes(bytes);

        assertThat(result).isEqualTo(data);
    }

    @Test
    void serialize_deserialize_empty_data_test() throws IOException {
        BTreeNodeData data = new BTreeNodeData();
        data.setNodeId(42);
        data.setLevel(0);

        byte[] bytes = BTreeNodeDataConverter.toBytes(data);
        BTreeNodeData result = BTreeNodeDataConverter.fromBytes(bytes);

        assertThat(result).isEqualTo(data);
    }

    @Test
    void serialize_deserialize_inner_data_test() throws IOException {
        BTreeNodeData data = new BTreeNodeData();
        data.setNodeId(42);
        data.setLevel(3);

        data.setKeys(listOf("Jack Nicholson", "Sean Connery", "Tom Hanks", "José Luis Alcaine"));
        data.setValues(listOf(listOf(4000000L), listOf(2000000L), listOf(1000000L), listOf(3000000L)));
        data.setChildrenPositions(listOf(0, 1, 2, 3, 4));
        data.setChildrenNodeIds(listOf(1993, 1994, 1995, 1996, 1997));

        byte[] bytes = BTreeNodeDataConverter.toBytes(data);
        BTreeNodeData result = BTreeNodeDataConverter.fromBytes(bytes);

        assertThat(result).isEqualTo(data);
    }

    @Test
    void serialize_deserialize_leaf_data_with_multiple_values_test() throws IOException {
        BTreeNodeData data = new BTreeNodeData();
        data.setNodeId(42);
        data.setLevel(3);

        data.setKeys(listOf("Gary Oldman", "Leonardo DiCaprio", "Robert De Niro", "Tom Hanks"));
        data.setValues(listOf(listOf(1000000L), listOf(2000000L, 3000000L, 4000000L), listOf(6000000L, 7000000L), listOf(5000000L)));
        data.setChildrenPositions(listOf(1, 2, 3, 4, 5));
        data.setChildrenNodeIds(listOf(1983, 1984, 1985, 1986, 1987));

        byte[] bytes = BTreeNodeDataConverter.toBytes(data);
        BTreeNodeData result = BTreeNodeDataConverter.fromBytes(bytes);

        assertThat(result).isEqualTo(data);
    }
}
