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
        addAndSearchValuesTest(3, 4096);
        addAndSearchValuesTest(7, 777);
        addAndSearchValuesTest(501, 10000);
        addAndSearchValuesTest(1000, 10000);
    }


    @Test
    void delete_test1() {
/*
        addAndDelete(
                Arrays.asList(4, 2, 0, 3, 5, 8, 9, 7, 6, 1),
                Arrays.asList(4, 1, 5, 2, 7, 9, 0, 3, 6, 8)
        );

*/
        addAndDelete(
                Arrays.asList(2, 9, 6, 3, 8, 5, 1, 0, 7, 4),
                Arrays.asList(1, 4, 0, 7, 6, 3, 2, 9, 5, 8)
        );

        addAndDelete(
                Arrays.asList(8, 21, 15, 6, 3, 7, 20, 2, 11, 4, 5, 0, 18, 16, 17, 1, 10, 13, 12, 14, 19, 9),
                Arrays.asList(13, 15, 20, 0, 4, 9, 12, 7, 8, 16, 19, 10, 21, 5, 11, 14, 18, 17, 1, 3, 2, 6)
        );
    }

    void addAndDelete(List<Integer> toAdd, List<Integer> toDelete) {
        NiceStringBTree tree = new NiceStringBTree(3);

        for(Integer key : toAdd) {
            tree.add(key.toString(), (long)key);
        }

        for (Integer value : toDelete) {
            printTree(tree);
            System.out.println("delete " + value);
            System.out.println();
            System.out.println("------------------------------------");
            tree.delete(value.toString());
        }

        System.out.println("____________________________________________________________");
    }

    @Test
    void delete_test() {
        //проверить удаление рута
        //проверить руда без ключей но с 2 детьми
        //for(int i = 0; i < 100; i++) {
            addAndDeleteValuesTest(3, 1);
            addAndDeleteValuesTest(3, 2);
            addAndDeleteValuesTest(3, 3);
            addAndDeleteValuesTest(3, 4);
            addAndDeleteValuesTest(3, 5);
            addAndDeleteValuesTest(3, 6);
            addAndDeleteValuesTest(3, 10);
            addAndDeleteValuesTest(3, 19);

            addAndDeleteValuesTest(4, 1);
            addAndDeleteValuesTest(4, 2);
            addAndDeleteValuesTest(4, 3);
            addAndDeleteValuesTest(4, 4);
            addAndDeleteValuesTest(4, 5);
            addAndDeleteValuesTest(4, 6);
            addAndDeleteValuesTest(4, 10);
            addAndDeleteValuesTest(4, 22);
        //}
        //addAndDeleteValuesTest(3, 777);
        //addAndDeleteValuesTest(500, 10000);
        //addAndDeleteValuesTest(1000, 10000);
    }

    private void addAndDeleteValuesTest(int m, int count) {
        NiceStringBTree tree = new NiceStringBTree(m);

        add(tree, count);

        List<Integer> values = getShuffledIntegerStream(count);

        System.out.println("deleted: " + values);
        try {
            for (Integer value : values) {
                tree.delete(value.toString());
            }
        } catch (Exception e) {
            System.out.println(values);
            throw e;
        }
        System.out.println();
    }

    private void addAndSearchValuesTest(int m, int count) {
        NiceStringBTree tree = new NiceStringBTree(m);

        add(tree, count);

        List<Integer> searchValues = getShuffledIntegerStream(count);
        for(Integer key : searchValues) {
            long value = key;

            assertThat(tree.search(key.toString()))
                    .as("value " + value + " must be found by key " + key)
                    .containsExactly(value);
        }
    }

    private void add(NiceStringBTree tree, int count) {
        List<Integer> list = getShuffledIntegerStream(count);
        System.out.println("added: " + list);
        for(Integer key : list) {
            tree.add(key.toString(), (long)key);
        }
    }

    private List<Integer> getShuffledIntegerStream(int count) {
        List<Integer> integers = IntStream.range(0, count)
                .boxed()
                .collect(Collectors.toList());
        Collections.shuffle(integers);
        return integers;
    }

    private <T> List<T> listOf(T... values) {
        return new ArrayList<>(Arrays.asList(values));
    }

    private void printTree(NiceStringBTree btree) {
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
