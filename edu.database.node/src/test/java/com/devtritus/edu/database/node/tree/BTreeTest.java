package com.devtritus.edu.database.node.tree;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class BTreeTest {

    @Test
    void add_and_search_values_test() {
        addThenSearchTest(3, 4096);
        addThenSearchTest(7, 777);
        addThenSearchTest(501, 10000);
        addThenSearchTest(1000, 10000);
    }

    @Test
    void delete_test_fixed_cases() {

        addThenDeleteTest(
                3,
                Arrays.asList(1, 2, 0),
                Arrays.asList(1, 2, 0)
        );

        addThenDeleteTest(
                3,
                Arrays.asList(4, 2, 0, 3, 5, 8, 9, 7, 6, 1),
                Arrays.asList(4, 1, 5, 2, 7, 9, 0, 3, 6, 8)
        );

        addThenDeleteTest(
                3,
                Arrays.asList(2, 9, 6, 3, 8, 5, 1, 0, 7, 4),
                Arrays.asList(1, 4, 0, 7, 6, 3, 2, 9, 5, 8)
        );

        addThenDeleteTest(
                3,
                Arrays.asList(8, 21, 15, 6, 3, 7, 20, 2, 11, 4, 5, 0, 18, 16, 17, 1, 10, 13, 12, 14, 19, 9),
                Arrays.asList(13, 15, 20, 0, 4, 9, 12, 7, 8, 16, 19, 10, 21, 5, 11, 14, 18, 17, 1, 3, 2, 6)
        );
    }

    @Test
    void delete_test_random_cases() {
        for(int i = 0; i < 50; i++) {
            addThenDeleteTest(3, 1);
            addThenDeleteTest(3, 2);
            addThenDeleteTest(3, 3);
            addThenDeleteTest(3, 4);
            addThenDeleteTest(3, 5);
            addThenDeleteTest(3, 6);
            addThenDeleteTest(3, 10);
            addThenDeleteTest(3, 19);
            addThenDeleteTest(3, 42);
            addThenDeleteTest(3, 777);

            addThenDeleteTest(4, 1);
            addThenDeleteTest(4, 2);
            addThenDeleteTest(4, 3);
            addThenDeleteTest(4, 4);
            addThenDeleteTest(4, 5);
            addThenDeleteTest(4, 6);
            addThenDeleteTest(4, 10);
            addThenDeleteTest(4, 42);
            addThenDeleteTest(4, 1103);

            addThenDeleteTest(500, 10000);
            addThenDeleteTest(1000, 10000);
        }
    }

    private void addThenSearchTest(int m, int count) {
        StringLongBTree tree = new StringLongBTree(m);

        List<Integer> toAdd = getShuffledIntegerStream(count);
        List<Integer> toSearch = getShuffledIntegerStream(count);

        try {
            add(tree, toAdd);
        } catch (Exception e) {
            System.out.println("to add: " + toAdd);
            throw e;
        }

        for(Integer key : toSearch) {
            long value = key;

            assertThat(tree.search(key.toString()))
                    .as("value " + value + " must be found by key " + key)
                    .containsExactly(value);
        }
    }

    private void addThenDeleteTest(int m, int count) {

        List<Integer> toAdd = getShuffledIntegerStream(count);
        List<Integer> toDelete = getShuffledIntegerStream(count);

        addThenDeleteTest(m, toAdd, toDelete);
    }

    void addThenDeleteTest(int m, List<Integer> toAdd, List<Integer> toDelete) {
        StringLongBTree tree = new StringLongBTree(m);

        try {
            add(tree, toAdd);
            delete(tree, toDelete);
        } catch (Exception e) {
            System.out.println("to add: " + toAdd);
            System.out.println("to delete: " + toDelete);
            throw e;
        }

        assertThat(tree.isEmpty());

        //System.out.println("END\n-------------------------------------------------\n");
    }

    private void add(StringLongBTree tree, List<Integer> toAdd) {
        for(Integer key : toAdd) {
            //printTree(tree);
            //System.out.println("added " + key + "\n");
            tree.add(key.toString(), (long)key);
        }
    }

    private void delete(StringLongBTree tree, List<Integer> toDelete) {
        for(Integer key : toDelete) {
            //printTree(tree);
            //System.out.println("delete " + key + "\n");
            tree.delete(key.toString());
        }
    }

    private List<Integer> getShuffledIntegerStream(int count) {
        List<Integer> integers = IntStream.range(0, count)
                .boxed()
                .collect(Collectors.toList());
        Collections.shuffle(integers);
        return integers;
    }

    private void printTree(StringLongBTree btree) {
        Map<Integer, List<List<String>>> map = new LinkedHashMap<>();
        flatTree(btree.root, map);
        for(Map.Entry<Integer, List<List<String>>> entry : map.entrySet()) {
            for(List<String> value : entry.getValue()) {
                System.out.print(value + " ");
            }
            System.out.print("\n\n");
        }
    }

    private void flatTree(BTreeNode<String, Long> node, Map<Integer, List<List<String>>> map) {
        List<List<String>> parentLevelList = map.computeIfAbsent(node.level, k -> new ArrayList<>());
        parentLevelList.add(node.getKeys());

        for(BTreeNode<String, Long> childNode : node.getChildren()) {
            flatTree(childNode, map);
        }
    }
}
