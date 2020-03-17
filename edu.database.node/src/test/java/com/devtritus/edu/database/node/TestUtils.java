package com.devtritus.edu.database.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class TestUtils {
    public static <T> List<T> listOf(T... elements) {
        return new ArrayList<>(Arrays.asList(elements));
    }
}
