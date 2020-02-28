package com.devtritus.edu.database.node.tree;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TreeUtilsTest {
    @Test
    void insert_element_to_list_test() {
        assertThat(TreeUtils.insert(listOf(), 1, 0)).containsExactly(1);
        assertThat(TreeUtils.insert(listOf(2), 1, 0)).containsExactly(1, 2);
        assertThat(TreeUtils.insert(listOf(1), 2, 1)).containsExactly(1, 2);
        assertThat(TreeUtils.insert(listOf(1, 3, 4), 2, 1)).containsExactly(1, 2, 3, 4);
        assertThat(TreeUtils.insert(listOf(1, 2, 3), 4, 3)).containsExactly(1, 2, 3, 4);
    }

    private <T> List<T> listOf(T... values) {
        return new ArrayList<>(Arrays.asList(values));
    }
}