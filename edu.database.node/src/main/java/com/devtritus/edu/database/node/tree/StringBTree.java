package com.devtritus.edu.database.node.tree;

import java.util.*;

public class StringBTree implements BTree<String, Long> {
    private Node cursor;

    StringBTree(int m) {
        cursor = new Node(m, 0);
    }

    @Override
    public String add(String key, Long value) {
        return selectRoot(cursor).add(key, value);
    }

    @Override
    public String delete(String key) {
        return selectRoot(cursor).delete(key);
    }

    @Override
    public List<Long> search(String key) {
        return cursor.search(key);
    }

    private Node selectRoot(Node node) {
        Node nextParent;
        Node currentNode = node;
        while((nextParent = currentNode.getParent()) != null) {
            currentNode = nextParent;
            currentNode.print("select as root");
            this.cursor = currentNode;
        }

        return currentNode;
    }

    private static class NodeData {
        private Long data;

        NodeData(Long data) {
            this.data = data;
        }
    }

    private static class Node implements BTree<String, Long> {
        private static int counter = 0;
        private final int nodeNumber = ++counter;

        private int m;
        private int level;
        private Node parent;
        private ArrayList<String> keys = new ArrayList<>();
        private Map<String, NodeData> values = new HashMap<>();
        private ArrayList<Node> children;

        Node(int m, int level) {
            this.m = m;
            this.level = level;
        }

        @Override
        public String add(String key, Long value) {
            NodeData newNodeData = new NodeData(value);

            keys.add(key);
            Collections.sort(keys);
            values.put(key, newNodeData);

            if(keys.size() == m) {
                int middleIndex = keys.size() / 2 + 1;
                String middleKey = keys.get(middleIndex);
                NodeData middleNodeData = values.get(middleKey);

                keys.remove(middleIndex);
                values.remove(middleKey);

                Node rightNode = new Node(m, level);

                rightNode.keys.addAll(keys.subList(middleIndex, keys.size()));

                HashMap<String, NodeData> rightNodeValues = new HashMap<>();
                for(String rightNodeKey : rightNode.keys) {
                    rightNodeValues.put(rightNodeKey, values.get(rightNodeKey));
                }

                rightNode.values = rightNodeValues;

                keys = new ArrayList<>(keys.subList(0, middleIndex));

                HashMap<String, NodeData> leftNodeValues = new HashMap<>();
                for(String leftNodeKey : keys) {
                    leftNodeValues.put(leftNodeKey, values.get(leftNodeKey));
                }

                values = leftNodeValues;

                if(parent == null) {
                    parent = new Node(m, level + 1);
                    parent.children = new ArrayList<>();
                    parent.children.add(this);
                }

                int rightNodeIndex = parent.children.indexOf(this);
                insertElement(parent.children, rightNode, rightNodeIndex);

                return parent.add(middleKey, middleNodeData.data);
            }

            return key;
        }

        static <T> void insertElement(ArrayList<T> list, T element, int insertIndex)  {
            T currentElement = element;
            for(int i = insertIndex; i < list.size() - 1; i++) {
                currentElement = list.set(insertIndex, currentElement);
            }

            list.add(currentElement);
        }

        @Override
        public String delete(String key) {
            return null;
        }

        @Override
        public List<Long> search(String key) {
            return null;
           /*
            for (int i = 0; i < keys.size() - 1; i++) {
                String nodeKey = keys.get(i);

                if (nodeKey.equals(key)) {
                    return Collections.singletonList(values.get(i).data);
                }
            }

            if (children.size() == 0) {
                return Collections.emptyList();
            }

            int largerElementIndex = Collections.binarySearch(keys, key);
            if (largerElementIndex != -1) {
                Node childNodeData = values.get(largerElementIndex).left;
                return childNodeData.search(key);
            } else {
                if (keys.size() > 0) {
                    //Берем последний, если элементов, которые "больше" нет, но список не пустой
                    Node rightChildNode = values.get(values.size() - 1).right;
                    return rightChildNode.search(key);
                } else {
                    return Collections.emptyList();
                }
            } */
        }

        public void print(String message) {
            System.out.format("[node-%s, level-%s] %s\n", nodeNumber, level, message);
        }

        private Node getParent() {
            return parent;
        }
    }
}
