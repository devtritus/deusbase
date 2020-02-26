package com.devtritus.edu.database.node.tree;

import org.junit.jupiter.api.Test;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class BTreeNodeBytesConverterTest {

    @Test
    void serialize_deserialize_leaf_node_test() throws IOException {
        BTreeNode<String, Integer> node = new BTreeNode<>(0);
        node.putKeyValue("Jack Nicholson", 3000000);
        node.putKeyValue("Sean Connery", 2000000);
        node.putKeyValue("Tom Hanks", 1000000);

        byte[] bytes = BTreeNodeBytesConverter.toBytes(node);
        BTreeNode<String, Integer> result = BTreeNodeBytesConverter.fromBytes(bytes);

        assertThat(result.getKeyValue(0)).isEqualTo(new Entry<>("Jack Nicholson", 3000000));
        assertThat(result.getKeyValue(1)).isEqualTo(new Entry<>("Sean Connery", 2000000));
        assertThat(result.getKeyValue(2)).isEqualTo(new Entry<>("Tom Hanks", 1000000));
    }
}
