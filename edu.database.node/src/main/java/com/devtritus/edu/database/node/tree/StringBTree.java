package com.devtritus.edu.database.node.tree;

import java.util.*;

public class StringBTree implements BTree<String, Long> {
    private Node cursor;

    StringBTree(int m) {
        if(m < 3) {
            throw new IllegalArgumentException("m must be more or equal then 3");
        }
        cursor = new Node(m, 0);
    }

    @Override
    public String add(String key, Long value) {
        getRoot().add(key, new NodeValue(value));
        return key;
    }

    @Override
    public String delete(String key) {
        getRoot().delete(key);
        return key;
    }

    @Override
    public List<Long> search(String key) {
        return getRoot().search(key);
    }

    Node getRoot() {
        Node nextParent;
        Node currentNode = cursor;
        while((nextParent = currentNode.parent) != null) {
            currentNode = nextParent;
            currentNode.print("select as root");
            cursor = currentNode;
        }

        return currentNode;
    }

    static class NodeValue {
        Long data;

        NodeValue(Long data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return data.toString();
        }
    }

    private static class Node {
        static int counter = 0;
        final int nodeNumber = ++counter;

        final int m;
        final int t;
        final  int level;
        final List<String> keys = new ArrayList<>();
        final Map<String, NodeValue> values = new HashMap<>();
        final List<Node> children = new ArrayList<>();
        Node parent;

        Node(int m, int level) {
            this.m = m;
            this.t = m / 2;
            this.level = level;
        }

        void add(String key, NodeValue nodeValue) {
            if(level == 0) {
                doAdd(key, nodeValue);
            } else {
                int index = determineKeyIndex(keys, key);
                if(index > -1) {
                    throw new IllegalStateException();
                } else {

                    Node childrenNode = children.get(Math.abs(index) - 1);
                    childrenNode.add(key, nodeValue);
                }
            }
        }

        void doAdd(String key, NodeValue nodeValue) {
            keys.add(key);
            Collections.sort(keys);
            values.put(key, nodeValue);

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
                NodeValue middleNodeValue = values.get(middleKey);

                nextParent.doAdd(middleKey, middleNodeValue);
            }

            if (keys.size() > m) {
                throw new IllegalStateException(key + " " + keys.size());
            }

            if (values.size() > m + 1) {
                throw new IllegalStateException(key + " " + values.size());
            }
        }

        Node copyNode(int startKey, int end, Node nextParent) {
            Node node = new Node(m, level);
            node.keys.addAll(new ArrayList<>(keys.subList(startKey, end)));

            for (String key : node.keys) {
                node.values.put(key, values.get(key));
            }

            if(!children.isEmpty()) {
                node.children.addAll(new ArrayList<>(children.subList(startKey, end + 1)));
            }

            node.parent = nextParent;

            return node;
        }

        List<Long> search(String key) {
            int index = determineKeyIndex(keys, key);
            if(index > -1) {
                return Collections.singletonList(values.get(key).data);
            } else {
                if(children.isEmpty()) {
                    return Collections.emptyList();
                }

                Node childrenNode = children.get(Math.abs(index) - 1);
                return childrenNode.search(key);
            }
        }

        void delete(String key) {
            delete(key, -1);
        }

        void delete(String key, int positionInParent) {
            int index = determineKeyIndex(keys, key);
            if(index > -1) {
                doDelete(key, positionInParent);
            } else if(!children.isEmpty()) {
                int childPosition = Math.abs(index) - 1;
                Node childrenNode = children.get(childPosition);
                childrenNode.delete(key, childPosition);
            }
        }

        void doDelete(String key, int nodePositionInParent) {
            int deletedKeyIndex = keys.indexOf(key);
            keys.remove(key);
            values.remove(key);

            if(level == 0) {
                if(keys.size() < t) {
                    if (parent != null) {
                        int rightNodePosition = nodePositionInParent + 1;
                        int leftNodePosition = nodePositionInParent - 1;
                        Node leftNode = null;

                        if(leftNodePosition > 0) {
                            leftNode = parent.children.get(leftNodePosition);
                        }

                        Node rightNode = null;
                        if(rightNodePosition < parent.children.size() -1) {
                             rightNode = parent.children.get(rightNodePosition);
                        }

                        if (rightNode != null && rightNode.keys.size() > t - 1) {
                            move(nodePositionInParent + 1, rightNode, 0);

                        } else if(leftNode != null && leftNode.keys.size() > t - 1) {
                            move(nodePositionInParent - 1, leftNode, leftNode.children.size() - 1);

                        } else if (rightNode != null) {
                            union(nodePositionInParent + 1, rightNode);

                        } else if (leftNode != null) {
                            union(nodePositionInParent - 1, leftNode);

                        } else {
                            throw new IllegalStateException(key + " " + nodePositionInParent);
                        }
                    }
                }
            } else {
                Node leftChildNode = children.get(deletedKeyIndex);
                Node rightChildNode = children.get(deletedKeyIndex + 1);

                if (leftChildNode.keys.size() > rightChildNode.keys.size()) {
                    grab(leftChildNode, deletedKeyIndex, leftChildNode.keys.size() - 1);
                } else {
                    grab(rightChildNode, deletedKeyIndex, 0);
                }
            }
        }

        void grab(Node nodeToGrab, int deletedKeyIndex, int grabKeyIndex) {
            String grabKey = nodeToGrab.keys.get(grabKeyIndex);
            NodeValue grabValue = nodeToGrab.values.get(grabKey);
            insertElement(keys, grabKey, deletedKeyIndex);
            values.put(grabKey, grabValue);

            nodeToGrab.doDelete(grabKey, grabKeyIndex);
        }

        void move(int parentKeyIndex, Node donatingNode, int donatingKeyIndex) {
            String parentKey = parent.keys.get(parentKeyIndex);
            NodeValue parentValue = parent.values.get(parentKey);
            keys.add(parentKey);
            values.put(parentKey, parentValue);

            String donatingNodeKey = donatingNode.keys.get(donatingKeyIndex);
            insertElement(parent.keys, donatingNodeKey, parentKeyIndex);
            NodeValue value = donatingNode.values.get(donatingNodeKey);
            parent.values.put(donatingNodeKey, value);

            donatingNode.keys.remove(donatingNodeKey);
            donatingNode.values.remove(donatingNodeKey);
        }

        void union(int parentKeyIndex, Node nodeToUnion) {
            Node unionNode = createUnionNode(nodeToUnion, this, m, level);

            deleteChildNode(parent, nodeToUnion);
            deleteChildNode(parent, this);

            String parentKey = parent.keys.get(parentKeyIndex);
            NodeValue parentValue = parent.values.get(parentKey);
            unionNode.keys.add(parentKey);
            unionNode.values.put(parentKey, parentValue);
            Collections.sort(unionNode.keys);

            insertElement(parent.children, unionNode, parentKeyIndex);

            parent.keys.remove(parentKey);
            parent.values.remove(parentKey);
        }

        static void deleteChildNode(Node parentNode, Node childNode) {
            boolean result = parentNode.children.remove(childNode);
            if(!result) {
                throw new IllegalStateException(childNode.toString());
            }
        }

        static Node createUnionNode(Node node1, Node node2, int m, int level) {
            if (node1.level != node2.level || node1.level != level) {
                throw new IllegalStateException();
            }

            Node unionNode = new Node(m, level);
            unionNode.keys.addAll(node1.keys);
            unionNode.keys.addAll(node2.keys);

            for(String key : node1.keys) {
                unionNode.values.put(key, node1.values.get(key));
            }

            for(String key : node2.keys) {
                unionNode.values.put(key, node2.values.get(key));
            }

            return unionNode;
        }

        static int determineKeyIndex(List<String> keys, String key) {
            return Collections.binarySearch(keys, key);
        }

        static void appendNodeToParent(Node parent, Node child, int insertionIndex) {
            insertElement(parent.children, child, insertionIndex);
        }

        void print(String message) {
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
