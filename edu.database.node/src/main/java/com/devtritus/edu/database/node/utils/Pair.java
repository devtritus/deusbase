package com.devtritus.edu.database.node.utils;

import java.util.Objects;

public class Pair<T, T1> {
    public final T first;
    public final T1 second;

    public Pair(T first, T1 second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> entry = (Pair<?, ?>) o;
        return Objects.equals(first, entry.first) &&
                Objects.equals(second, entry.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return "{ " + first + ", " + second + " }";
    }
}
