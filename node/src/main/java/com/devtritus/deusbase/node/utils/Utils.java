package com.devtritus.deusbase.node.utils;

import java.nio.charset.StandardCharsets;
import java.util.List;

public abstract class Utils {
    public static <T> List<T> insertToList(List<T> list, T element, int insertIndex)  {
        T current = element;
        for(int i = insertIndex; i < list.size(); i++) {
            current = list.set(i, current);
        }

        list.add(current);

        return list;
    }

    public static <T> List<T> deleteFromList(List<T> list, int deleteIndex) {
        if(list.isEmpty()) {
            return list;
        }

        for(int i = deleteIndex + 1; i < list.size(); i++) {
            T nextElement = list.get(i);
            list.set(i - 1, nextElement);
        }
        list.remove(list.size() - 1);

        return list;
    }

    public static byte[] utf8StringToBytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    public static String bytesToUtf8String(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
