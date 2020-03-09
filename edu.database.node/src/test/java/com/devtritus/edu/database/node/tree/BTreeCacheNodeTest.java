package com.devtritus.edu.database.node.tree;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class BTreeCacheNodeTest {
    @Test
    void put_test() {
        BTreeNodeCache cache = new BTreeNodeCache(3);

        assertThat(cache.getCachedEntries()).hasSize(0);

        PathEntry<BTreeNode, String, Long, Integer> entry1 = entryOf(new BTreeNode(0, 0), 0);
        PathEntry<BTreeNode, String, Long, Integer> entry2 = entryOf(new BTreeNode(1, 1), 1);
        PathEntry<BTreeNode, String, Long, Integer> entry3 = entryOf(new BTreeNode(2, 2), 2);
        PathEntry<BTreeNode, String, Long, Integer> entry4 = entryOf(new BTreeNode(3, 3), 3);
        PathEntry<BTreeNode, String, Long, Integer> entry5 = entryOf(new BTreeNode(4, 4), 4);

        cache.put(entry1.value, entry1.key);
        cache.clearToLimit();
        assertCacheContent(cache, entry1);

        cache.put(entry2.value, entry2.key);
        cache.clearToLimit();
        assertCacheContent(cache, entry1, entry2);

        cache.put(entry3.value, entry3.key);
        cache.clearToLimit();
        assertCacheContent(cache, entry1, entry2, entry3);

        cache.put(entry4.value, entry4.key);
        cache.clearToLimit();
        assertCacheContent(cache, entry2, entry3, entry4);

        cache.put(entry3.value, entry3.key);
        cache.clearToLimit();
        assertCacheContent(cache, entry2, entry4, entry3);

        cache.put(entry5.value, entry5.key);
        cache.clearToLimit();
        assertCacheContent(cache, entry4, entry3, entry5);

        cache.put(entry1.value, entry1.key);
        cache.clearToLimit();
        assertCacheContent(cache, entry3, entry5, entry1);
    }

    @Test
    void get_test() {
        BTreeNodeCache cache = new BTreeNodeCache(3);

        assertThat(cache.getCachedEntries()).hasSize(0);

        BTreeNode node1 = new BTreeNode(0, 0);
        BTreeNode node2 = new BTreeNode(1, 1);

        cache.put(0, node1);
        cache.put(1, node2);

        assertThat(cache.get(-1)).isNull();
        assertThat(cache.get(0)).isEqualTo(node1);
        assertThat(cache.get(1)).isEqualTo(node2);
        assertThat(cache.get(3)).isNull();
    }

    private static void assertCacheContent(BTreeNodeCache cache, PathEntry<BTreeNode, String, Long, Integer>... entries) {
        List<PathEntry<BTreeNode, String, Long, Integer>> result = cache.getCachedEntries().entrySet().stream()
                .map(entry -> entryOf(entry.getValue(), entry.getKey()))
                .collect(Collectors.toList());

        assertThat(result).containsExactlyInAnyOrder(entries);
    }

    private static PathEntry<BTreeNode, String, Long, Integer> entryOf(BTreeNode node, int position) {
        return new PathEntry<>(node, position);
    }
}
