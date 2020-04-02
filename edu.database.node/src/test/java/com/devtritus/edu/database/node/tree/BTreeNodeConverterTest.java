package com.devtritus.edu.database.node.tree;

import org.junit.jupiter.api.Test;

import static com.devtritus.edu.database.node.TestUtils.listOf;
import static org.assertj.core.api.Assertions.assertThat;

class BTreeNodeConverterTest {

    @Test
    void convert_to_node_test() {
        BTreeNode node = new BTreeNode(42, 3);
        node.putKeyValue("Jack Nicholson", listOf(4000000L));
        node.putKeyValue("Sean Connery", listOf(2000000L));
        node.putKeyValue("Tom Hanks", listOf(1000000L));
        node.putKeyValue("José Luis Alcaine", listOf(3000000L));

        node.insertChild(0, 1993);
        node.insertChild(1, 1994);
        node.insertChild(2, 1995);
        node.insertChild(3, 1996);
        node.insertChild(4, 1997);

        BTreeNodeData data = BTreeNodeConverter.toNodeData(node);

        assertThat(data.getNodeId()).isEqualTo(42);
        assertThat(data.getLevel()).isEqualTo(3);
        assertThat(data.getKeys()).containsExactly("Jack Nicholson", "José Luis Alcaine", "Sean Connery", "Tom Hanks");
        data.setValues(listOf(listOf(4000000L), listOf(3000000L), listOf(2000000L), listOf(1000000L)));
        assertThat(data.getChildrenNodeIds()).containsExactly(1993, 1994, 1995, 1996, 1997);
        assertThat(data.getChildrenPositions()).isEmpty();
    }

    @Test
    void convert_from_node_test() {
        BTreeNodeData data = new BTreeNodeData();
        data.setNodeId(42);
        data.setLevel(3);

        data.setKeys(listOf("Gary Oldman", "Leonardo DiCaprio", "Robert De Niro", "Tom Hanks"));
        data.setValues(listOf(listOf(1000000L), listOf(2000000L, 3000000L, 4000000L), listOf(6000000L, 7000000L), listOf(5000000L)));
        data.setChildrenNodeIds(listOf(1983, 1984, 1985, 1986, 1987));

        BTreeNode node = BTreeNodeConverter.fromNodeData(data);

        assertThat(node.getNodeId()).isEqualTo(42);
        assertThat(node.getLevel()).isEqualTo(3);
        assertThat(node.getKeyValue(0)).isEqualTo(new Pair<>("Gary Oldman", listOf(1000000L)));
        assertThat(node.getKeyValue(1)).isEqualTo(new Pair<>("Leonardo DiCaprio", listOf(2000000L, 3000000L, 4000000L)));
        assertThat(node.getKeyValue(2)).isEqualTo(new Pair<>("Robert De Niro", listOf(6000000L, 7000000L)));
        assertThat(node.getKeyValue(3)).isEqualTo(new Pair<>("Tom Hanks", listOf(5000000L)));
        assertThat(node.getChildren()).containsExactly(1983, 1984, 1985, 1986, 1987);
    }
}
