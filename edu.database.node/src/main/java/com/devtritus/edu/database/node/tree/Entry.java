package com.devtritus.edu.database.node.tree;

import java.util.Objects;

class Entry<T, T1> {
    final T key;
    final T1 value;

    Entry(T key, T1 value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entry<?, ?> entry = (Entry<?, ?>) o;
        return Objects.equals(key, entry.key) &&
                Objects.equals(value, entry.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "{ " + key + ", " + value + " }";
    }
}
