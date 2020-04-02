package com.devtritus.edu.database.node.tree;

import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class BTreeTest {

    @Test
    void add_then_serialize_then_search_by_keys_test() {
        String fileName = "test.index";
        clearFile(fileName);
        BTreeNodeDiskManager manager = new BTreeNodeDiskManager(fileName);
        int m = manager.initialize();

        BTreeNodeDiskProvider provider = manager.getNodeProvider();

        BTreeImpl tree = new BTreeImpl(m, provider);

        List<String> toAdd = getRandomStrings(50, 250, 10000);

        Map<String, Long> toAddMap = new HashMap<>();
        for(int i = 0; i < toAdd.size(); i++) {
            toAddMap.put(toAdd.get(i), (long)i);
        }

        try {
            add(tree, toAddMap, provider);
        } catch (Exception e) {
            System.out.println("to add: " + toAddMap);
            throw e;
        }

        provider.clearCache();

        for (Map.Entry<String, Long> entry : toAddMap.entrySet()) {
            List<Long> valueList = tree.searchByKey(entry.getKey());
            assertThat(valueList).containsExactly(entry.getValue());
        }
    }

    @Test
    void add_then_search_by_keys_test() {
        addThenSearchByKeyTest(3, 4096);
        addThenSearchByKeyTest(7, 777);
        addThenSearchByKeyTest(501, 10000);
        addThenSearchByKeyTest(1000, 10000);
    }

    @Test
    void search_list_test() {
        BTreeNodeInMemoryProvider provider = new BTreeNodeInMemoryProvider();
        BTreeImpl tree = new BTreeImpl(3, provider);

        List<String> keys = Arrays.asList("a", "aa", "aaa", "b", "bb", "bbb", "abb", "bba", "aab", "baa", "aaaa", "aaab",
                "bbbba", "aabb", "bbaa", "baba", "abab", "baab", "abba", "babb", "abaa", "bbab", "aaba", "bbbb", "bbba");

        for (int i = 0; i < keys.size() - 1; i++) {
            tree.add(keys.get(i), Collections.singletonList((long)i));
            //printTree(provider);
        }

        Map<String, List<Long>> result = tree.fetch("aaaa");
        assertThat(result).containsOnlyKeys("aaaa");

        result = tree.fetch("bba");
        assertThat(result).containsOnlyKeys("bba", "bbaa", "bbab");

        result = tree.fetch("ba");
        assertThat(result).containsOnlyKeys("baba", "babb", "baa", "baab");

        result = tree.fetch("aaaa");
        assertThat(result).containsOnlyKeys("aaaa");

        result = tree.fetch("a");
        assertThat(result).containsOnlyKeys("a", "aa", "aaa", "abb", "aab", "aaaa", "aaab", "aabb", "abab", "abba", "abaa", "aaba");
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
        for (int i = 0; i < 50; i++) {
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

    @Test
    void delete_result_test() {
        BTreeNodeInMemoryProvider provider = new BTreeNodeInMemoryProvider();
        BTreeImpl tree = new BTreeImpl(3, provider);

        add(tree, createKeyValueMap(Arrays.asList(1, 2, 3, 4, 5)), provider);

        assertThat(tree.deleteKey("5")).isTrue();
        assertThat(tree.deleteKey("5")).isFalse();
    }

    //TODO: add timer, ignore tests
    //@Test
    void add_test_big_random_case() {
        addThenSearchByKeyTest(200, 1000000);
    }

    //@Test
    void delete_test_big_random_case() {
        addThenDeleteTest(200, 1000000);
    }

    private void addThenSearchByKeyTest(int m, int count) {
        BTreeNodeInMemoryProvider provider = new BTreeNodeInMemoryProvider();
        BTreeImpl tree = new BTreeImpl(m, provider);

        List<Integer> toAdd = getShuffledIntegerStream(count);
        Map<String, Long> toAddMap = createKeyValueMap(toAdd);

        try {
            add(tree, createKeyValueMap(toAdd), provider);
        } catch (Exception e) {
            System.out.println("to add: " + toAdd);
            throw e;
        }

        for(Map.Entry<String, Long> entry : toAddMap.entrySet()) {
            assertThat(tree.searchByKey(entry.getKey()))
                    .as("value " + entry.getValue() + " must be found by key " + entry.getKey())
                    .containsOnly(entry.getValue());
        }
    }

    private void addThenDeleteTest(int m, int count) {

        List<Integer> toAdd = getShuffledIntegerStream(count);
        List<Integer> toDelete = getShuffledIntegerStream(count);

        addThenDeleteTest(m, toAdd, toDelete);
    }

    void addThenDeleteTest(int m, List<Integer> toAdd, List<Integer> toDelete) {
        BTreeNodeInMemoryProvider provider = new BTreeNodeInMemoryProvider();
        BTreeImpl tree = new BTreeImpl(m, provider);

        try {
            add(tree, createKeyValueMap(toAdd), provider);
            delete(tree, mapToStrings(toDelete), provider);
        } catch (Exception e) {
            System.out.println("to add: " + toAdd);
            System.out.println("to delete: " + toDelete);
            throw e;
        }

        assertThat(tree.isEmpty());

        //System.out.println("END\n-------------------------------------------------\n");
    }

    private void add(BTreeImpl tree, Map<String, Long> toAdd, BTreeNodeProvider<BTreeNode, String, List<Long>, Integer> provider) {
        for(Map.Entry<String, Long> entry : toAdd.entrySet()) {
            //printTree(provider);
            //System.out.println("add " + pair.first + "\n");
            tree.add(entry.getKey(), Collections.singletonList(entry.getValue()));
            List<Long> valueList = tree.searchByKey(entry.getKey());
            assertThat(valueList).containsExactly(entry.getValue());
        }
    }

    private void delete(BTreeImpl tree, List<String> toDelete, BTreeNodeProvider<BTreeNode, String, List<Long>, Integer> provider) {
        for(String key : toDelete) {
            //printTree(provider);
            //System.out.println("delete " + key + "\n");
            boolean result = tree.deleteKey(key);
            assertThat(result).isTrue();
        }
    }

    private List<String> getRandomStrings(int minLength, int maxLength, int count) {

        int leftLimit = 97; // 'a'
        int rightLimit = 122; // 'z'

        List<String> strings = new ArrayList<>();
        Random random = new Random();
        for(int i = 0; i < count; i++) {
            String generatedString = random.ints(leftLimit, rightLimit + 1)
                    .limit(minLength + random.nextInt(maxLength - minLength))
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();

            strings.add(generatedString);
        }

        return strings;
    }

    private List<Integer> getShuffledIntegerStream(int count) {
        List<Integer> integers = IntStream.range(0, count)
                .boxed()
                .collect(Collectors.toList());
        Collections.shuffle(integers);
        return integers;
    }

    private void printTree(BTreeNodeProvider<BTreeNode, String, List<Long>, Integer> provider) {
        Map<Integer, List<List<String>>> map = new LinkedHashMap<>();
        flatTree(provider.getRootNode(), map, provider);
        for(Map.Entry<Integer, List<List<String>>> entry : map.entrySet()) {
            for(List<String> value : entry.getValue()) {
                System.out.print(value + " ");
            }
            System.out.print("\n\n");
        }
    }

    private void flatTree(BTreeNode node, Map<Integer, List<List<String>>> map, BTreeNodeProvider<BTreeNode, String, List<Long>, Integer> provider) {
        List<List<String>> parentLevelList = map.computeIfAbsent(node.getLevel(), k -> new ArrayList<>());
        parentLevelList.add(node.getKeys());

        for(BTreeNode childNode : provider.getNodes(node.getChildren())) {
            flatTree(childNode, map, provider);
        }
    }

    private void clearFile(String name) {
        try(FileOutputStream writer = new FileOutputStream(name)) {
            writer.write(("").getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> Map<String, Long> createKeyValueMap(List<T> list) {
        Map<String, Long> map = new HashMap<>();
        for(int i = 0; i < list.size(); i++) {
            map.put(list.get(i).toString(), (long) i);
        }

        return map;
    }

    private static <T> List<String> mapToStrings(List<T> list) {
        return list.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    //Tasks

    /*
        1. Implement a cache +
        2. Exclude unnecessary writing/reading operations +
        3. Find a solution for equals keys +
        4. Resolve a problem with overflow of node's block -
        5. Optimize a size of index +
        6. Improve the terminal
        7. Refactoring
        8. Think about concurrent access
     */
}
