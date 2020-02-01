package com.devtritus.edu.database.node.tree;

import java.util.*;

public class StringBTree implements BTree<String, Long> {
    private Node cursor;

    StringBTree(int m) {
        cursor = new Node(m, 0);
    }

    @Override
    public String add(String key, Long value) {
        return getRoot().add(key, new NodeData(value));
    }

    @Override
    public String delete(String key) {
        return getRoot().delete(key);
    }

    @Override
    public List<Long> search(String key) {
        return getRoot().search(key);
    }

    int getTreeLevel() {
        return getRoot().level;
    }

    private Node getRoot() {
        Node nextParent;
        Node currentNode = cursor;
        while((nextParent = currentNode.parent) != null) {
            currentNode = nextParent;
            currentNode.print("select as root");
            cursor = currentNode;
        }

        return currentNode;
    }

    private static class NodeData {
        private Long data;

        NodeData(Long data) {
            this.data = data;
        }
    }

    private static class Node {
        private static int counter = 0;
        private final int nodeNumber = ++counter;

        private final int m;
        private final  int level;
        private final List<String> keys = new ArrayList<>();
        private final Map<String, NodeData> keyToValueMap = new HashMap<>();
        private final List<Node> children = new ArrayList<>();
        private Node parent;

        Node(int m, int level) {
            this.m = m;
            this.level = level;
        }

        public String add(String key, NodeData nodeData) {
            if(level == 0) {
                return doAdd(key, nodeData);
            } else {
                int index = Collections.binarySearch(keys, key);
                if(index > -1) {
                    throw new IllegalStateException();
                } else {

                    Node childrenNode = children.get(Math.abs(index) - 1);
                    return childrenNode.add(key, nodeData);
                }
            }
        }

        public String delete(String key) {
            return null;
        }

        public List<Long> search(String key) {
            int index = Collections.binarySearch(keys, key);
            if(index > -1) {
                return Collections.singletonList(keyToValueMap.get(key).data);
            } else {
                if(children.isEmpty()) {
                    return Collections.emptyList();
                }

                Node childrenNode = children.get(Math.abs(index) - 1);
                return childrenNode.search(key);
            }
        }

        String doAdd(String key, NodeData nodeData) {
            keys.add(key);
            Collections.sort(keys);
            keyToValueMap.put(key, nodeData);

            if (keys.size() == m) {
                int middleIndex = keys.size() / 2;

                int insertionIndex;
                Node nextParent;
                if(parent == null) {
                    nextParent = new Node(m, level + 1);
                    insertionIndex = 0;
                } else {
                    nextParent = parent;
                    insertionIndex = parent.children.indexOf(this);
                    parent.children.remove(this);
                    if(insertionIndex == -1) {
                        throw new IllegalStateException();
                    }
                }

                Node leftNode = copyNode(0, middleIndex, nextParent);
                Node rightNode = copyNode(middleIndex + 1, keys.size(), nextParent);

                appendNodeToParent(nextParent, leftNode, insertionIndex);
                appendNodeToParent(nextParent, rightNode, insertionIndex + 1);

                String middleKey = keys.get(middleIndex);
                NodeData middleNodeData = keyToValueMap.get(middleKey);

                return nextParent.doAdd(middleKey, middleNodeData);
            }

            return key;
        }

        private Node copyNode(int startKey, int end, Node nextParent) {
            Node node = new Node(m, level);
            node.keys.addAll(new ArrayList<>(keys.subList(startKey, end)));

            for (String key : node.keys) {
                node.keyToValueMap.put(key, keyToValueMap.get(key));
            }

            if(!children.isEmpty()) {
                node.children.addAll(new ArrayList<>(children.subList(startKey, end + 1)));
            }

            node.parent = nextParent;

            return node;
        }

        private static void appendNodeToParent(Node parent, Node child, int insertionIndex) {
            insertElement(parent.children, child, insertionIndex);
        }

        private void print(String message) {
            System.out.format("[node-%s, level-%s] %s\n", nodeNumber, level, message);
        }

        @Override
        public String toString() {
            return "[ " + String.join(", ", keys) + " ]";
        }
    }

    @Override
    public String toString() {
        return getRoot().toString();
    }

    static <T> List<T> insertElement(List<T> list, T element, int insertIndex)  {
        T currentElement = element;
        for(int i = insertIndex; i < list.size(); i++) {
            currentElement = list.set(i, currentElement);
        }

        list.add(currentElement);

        return list;
    }
}
