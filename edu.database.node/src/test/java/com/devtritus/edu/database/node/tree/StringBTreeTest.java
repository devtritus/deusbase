package com.devtritus.edu.database.node.tree;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StringBTreeTest {
    @Test
    void insert_element_to_list_test() {
        assertThat(StringBTree.insertElement(listOf(), 1, 0)).containsExactly(1);
        assertThat(StringBTree.insertElement(listOf(2), 1, 0)).containsExactly(1, 2);
        assertThat(StringBTree.insertElement(listOf(1), 2, 1)).containsExactly(1, 2);
        assertThat(StringBTree.insertElement(listOf(1, 3, 4), 2, 1)).containsExactly(1, 2, 3, 4);
        assertThat(StringBTree.insertElement(listOf(1, 2, 3), 4, 3)).containsExactly(1, 2, 3, 4);
    }

    @Test
    void add_and_search_values_test() {
        addAndSearchValuesTest(2, 4096);
        addAndSearchValuesTest(3, 777);
        addAndSearchValuesTest(500, 10000);
        addAndSearchValuesTest(1000, 10000);
    }

    private void addAndSearchValuesTest(int m, int count) {
        StringBTree tree = new StringBTree(m);
        for(int i = 0; i < count; i++) {
            tree.add(Integer.toString(i), (long)i);
        }

        for(int i = 0; i < count; i++) {
            String key = Integer.toString(i);
            long value = i;

            assertThat(tree.search(key))
                    .as("value " + value + " must be found by key " + key)
                    .containsExactly(value);
        }
    }

    @Test
    void delete_test() {
    }

    private <T> List<T> listOf(T... values) {
        return new ArrayList<>(Arrays.asList(values));
    }
}
