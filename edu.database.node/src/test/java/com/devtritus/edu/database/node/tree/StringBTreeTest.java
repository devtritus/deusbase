package com.devtritus.edu.database.node.tree;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        addAndSearchValuesTest(3, 20);
        addAndSearchValuesTest(3, 4096);
        addAndSearchValuesTest(3, 777);
        addAndSearchValuesTest(500, 10000);
        addAndSearchValuesTest(1000, 10000);
    }

    @Test
    void delete_test() {
        addAndDeleteValuesTest(3, 15);
        //addAndDeleteValuesTest(3, 777);
        //addAndDeleteValuesTest(500, 10000);
        //addAndDeleteValuesTest(1000, 10000);
    }

    private void addAndDeleteValuesTest(int m, int count) {
        StringBTree tree = new StringBTree(m);

        add(tree, count);

        List<Integer> values = getShuffledIntegerStream(count);

        for(Integer value : values) {
            tree.delete(value.toString());
        }
        System.out.println(tree);
    }

    private void addAndSearchValuesTest(int m, int count) {
        StringBTree tree = new StringBTree(m);

        add(tree, count);

        List<Integer> searchValues = getShuffledIntegerStream(count);
        for(Integer key : searchValues) {
            long value = key;

            assertThat(tree.search(key.toString()))
                    .as("value " + value + " must be found by key " + key)
                    .containsExactly(value);
        }
    }

    private void add(StringBTree tree, int count) {
        for(Integer key : getShuffledIntegerStream(count)) {
            tree.add(key.toString(), (long)key);
        }
    }

    private List<Integer> getShuffledIntegerStream(int count) {
        List<Integer> integers = IntStream.range(0, count)
                .boxed()
                .collect(Collectors.toList());
        //Collections.shuffle(integers);
        return integers;
    }

    private <T> List<T> listOf(T... values) {
        return new ArrayList<>(Arrays.asList(values));
    }
}
