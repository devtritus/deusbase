package com.devtritus.edu.database.node.tree;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BTreeCacheNodeTest {
    @Test
    void put_test() {
        BTreeNodeCache cache = new BTreeNodeCache(3);

        assertThat(cache.getCachedNodes()).hasSize(0);

        BTreeNode node1 = new BTreeNode(0, 0);
        BTreeNode node2 = new BTreeNode(1, 1);
        BTreeNode node3 = new BTreeNode(2, 2);
        BTreeNode node4 = new BTreeNode(3, 3);
        BTreeNode node5 = new BTreeNode(4, 4);

        cache.put(node1);
        cache.clearToLimit();
        assertCacheContent(cache, node1);

        cache.put(node2);
        cache.clearToLimit();
        assertCacheContent(cache, node1, node2);

        cache.put(node3);
        cache.clearToLimit();
        assertCacheContent(cache, node1, node2, node3);

        cache.put(node4);
        cache.clearToLimit();
        assertCacheContent(cache, node2, node3, node4);

        cache.put(node3);
        cache.clearToLimit();
        assertCacheContent(cache, node2, node4, node3);

        cache.put(node5);
        cache.clearToLimit();
        assertCacheContent(cache, node4, node3, node5);

        cache.put(node1);
        cache.clearToLimit();
        assertCacheContent(cache, node3, node5, node1);
    }

    @Test
    void get_test() {
        BTreeNodeCache cache = new BTreeNodeCache(3);

        assertThat(cache.getCachedNodes()).hasSize(0);

        BTreeNode node1 = new BTreeNode(0, 0);
        BTreeNode node2 = new BTreeNode(1, 1);

        cache.put(node1);
        cache.put(node2);

        assertThat(cache.get(-1)).isNull();
        assertThat(cache.get(0)).isEqualTo(node1);
        assertThat(cache.get(1)).isEqualTo(node2);
        assertThat(cache.get(3)).isNull();
    }

    @Test
    void delete_test() {
        BTreeNodeCache cache = new BTreeNodeCache(3);

        assertThat(cache.getCachedNodes()).hasSize(0);

        BTreeNode node1 = new BTreeNode(0, 0);
        BTreeNode node2 = new BTreeNode(1, 1);

        cache.put(node1);
        cache.put(node2);

        cache.delete(0);
        assertThat(cache.get(0)).isNull();
        assertThat(cache.get(1)).isEqualTo(node2);

        cache.delete(1);
        assertThat(cache.get(0)).isNull();
        assertThat(cache.get(1)).isNull();
        assertThat(cache.getCachedNodes()).hasSize(0);
    }

    private static void assertCacheContent(BTreeNodeCache cache, BTreeNode... entries) {
        List<BTreeNode> result = new ArrayList<>(cache.getCachedNodes().values());

        assertThat(result).containsExactlyInAnyOrder(entries);
    }
}
