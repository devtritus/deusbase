package com.devtritus.edu.database.node.tree;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class StringBTreeTest {

    @Test
    void add_and_search_values_test() {
        addAndSearchTest(3, 4096);
        addAndSearchTest(7, 777);
        addAndSearchTest(501, 10000);
        addAndSearchTest(1000, 10000);
    }

    @Test
    void delete_test_fixed_cases() {

        addAndDeleteTest(
                3,
                Arrays.asList(1, 2, 0),
                Arrays.asList(1, 2, 0)
        );

        addAndDeleteTest(
                3,
                Arrays.asList(4, 2, 0, 3, 5, 8, 9, 7, 6, 1),
                Arrays.asList(4, 1, 5, 2, 7, 9, 0, 3, 6, 8)
        );

        addAndDeleteTest(
                3,
                Arrays.asList(2, 9, 6, 3, 8, 5, 1, 0, 7, 4),
                Arrays.asList(1, 4, 0, 7, 6, 3, 2, 9, 5, 8)
        );

        addAndDeleteTest(
                3,
                Arrays.asList(8, 21, 15, 6, 3, 7, 20, 2, 11, 4, 5, 0, 18, 16, 17, 1, 10, 13, 12, 14, 19, 9),
                Arrays.asList(13, 15, 20, 0, 4, 9, 12, 7, 8, 16, 19, 10, 21, 5, 11, 14, 18, 17, 1, 3, 2, 6)
        );
    }

    @Test
    void delete_test_random_cases() {
        for(int i = 0; i < 50; i++) {
            addAndDeleteTest(3, 1);
            addAndDeleteTest(3, 2);
            addAndDeleteTest(3, 3);
            addAndDeleteTest(3, 4);
            addAndDeleteTest(3, 5);
            addAndDeleteTest(3, 6);
            addAndDeleteTest(3, 10);
            addAndDeleteTest(3, 19);
            addAndDeleteTest(3, 42);
            addAndDeleteTest(3, 777);

            addAndDeleteTest(4, 1);
            addAndDeleteTest(4, 2);
            addAndDeleteTest(4, 3);
            addAndDeleteTest(4, 4);
            addAndDeleteTest(4, 5);
            addAndDeleteTest(4, 6);
            addAndDeleteTest(4, 10);
            addAndDeleteTest(4, 42);
            addAndDeleteTest(4, 1103);

            addAndDeleteTest(500, 10000);
            addAndDeleteTest(1000, 10000);
        }
    }

    private void addAndSearchTest(int m, int count) {
        StringBTree tree = new StringBTree(m);

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

    private void addAndDeleteTest(int m, int count) {

        List<Integer> toAdd = getShuffledIntegerStream(count);
        List<Integer> toDelete = getShuffledIntegerStream(count);

        addAndDeleteTest(m, toAdd, toDelete);
    }

    void addAndDeleteTest(int m, List<Integer> toAdd, List<Integer> toDelete) {
        StringBTree tree = new StringBTree(m);

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

    private void add(StringBTree tree, List<Integer> toAdd) {
        for(Integer key : toAdd) {
            //printTree(tree);
            //System.out.println("added " + key + "\n");
            tree.add(key.toString(), (long)key);
        }
    }

    private void delete(StringBTree tree, List<Integer> toDelete) {
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

    private void printTree(StringBTree btree) {
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
