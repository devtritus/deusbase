package com.devtritus.edu.database.node.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class StringBTree implements BTree<String, Long> {
    private Node root;

    StringBTree(int m) {
        root = new Node(m);
    }

    @Override
    public String add(String key, Long value) {
        KeyValue keyValue = new KeyValue(key, value);
        return root.add(keyValue);
    }

    @Override
    public String delete(String key) {
        return root.delete(key);
    }

    @Override
    public List<String> search(String key) {
        return root.search(key);
    }

    private static class Node {
        private TreeMap<String, Long> keyValueMap = new TreeMap<>();
        private int m;
        private Node parent;
        private List<Node> children = new ArrayList<>();

        Node(int m) {
            this(null, m);
        }

        Node(Node parent, int m) {
            this.m = m;
            this.parent = parent;
        }

        String add(KeyValue keyValue) {
            int size = keyValueMap.size();
            if(size > m - 1) {
                keyValueMap.put(keyValue.key, keyValue.value);
            } else {

            }
            return null;
        }

        String delete(String key) {
            return null;
        }

        List<String> search(String key) {
            return null;
        }
    }

    private static class KeyValue {
        private String key;
        private Long value;

        KeyValue(String key, Long value) {
            this.key = key;
            this.value = value;
        }
    }
}
