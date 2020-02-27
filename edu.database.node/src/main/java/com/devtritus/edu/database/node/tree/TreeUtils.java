package com.devtritus.edu.database.node.tree;

import java.util.List;

abstract class TreeUtils {
    static <T> List<T> insert(List<T> list, T element, int insertIndex)  {
        T current = element;
        for(int i = insertIndex; i < list.size(); i++) {
            current = list.set(i, current);
        }

        list.add(current);

        return list;
    }
}
