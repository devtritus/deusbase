package com.devtritus.edu.database.node.tree;

import java.util.List;

abstract class Utils {
    static <T> List<T> insertToList(List<T> list, T element, int insertIndex)  {
        T current = element;
        for(int i = insertIndex; i < list.size(); i++) {
            current = list.set(i, current);
        }

        list.add(current);

        return list;
    }

    static <T> List<T> deleteFromList(List<T> list, int deleteIndex) {
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
}
