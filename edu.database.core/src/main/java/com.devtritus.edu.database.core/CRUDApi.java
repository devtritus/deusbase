package com.devtritus.edu.database.core;

public interface CRUDApi<T, K> {
    K create(T object);
    T read(K key);
    K delete(K key);
    K update(T object, K key);
}
