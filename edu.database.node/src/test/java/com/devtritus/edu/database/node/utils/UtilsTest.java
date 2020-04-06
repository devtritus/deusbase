package com.devtritus.edu.database.node.utils;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UtilsTest {
    @Test
    void insert_element_to_list_test() {
        assertThat(Utils.insertToList(listOf(), 1, 0)).containsExactly(1);
        assertThat(Utils.insertToList(listOf(2), 1, 0)).containsExactly(1, 2);
        assertThat(Utils.insertToList(listOf(1), 2, 1)).containsExactly(1, 2);
        assertThat(Utils.insertToList(listOf(1, 3, 4), 2, 1)).containsExactly(1, 2, 3, 4);
        assertThat(Utils.insertToList(listOf(1, 2, 3), 4, 3)).containsExactly(1, 2, 3, 4);
    }

    @Test
    void delete_element_from_list_test() {
        assertThat(Utils.deleteFromList(listOf(1), 0)).containsExactly();
        assertThat(Utils.deleteFromList(listOf(1, 2, 3), 1)).containsExactly(1, 3);
        assertThat(Utils.deleteFromList(listOf(1, 2, 3, 4), 0)).containsExactly(2, 3, 4);
        assertThat(Utils.deleteFromList(listOf(1, 2, 3, 4), 3)).containsExactly(1, 2, 3);
    }

    private <T> List<T> listOf(T... values) {
        return new ArrayList<>(Arrays.asList(values));
    }
}
