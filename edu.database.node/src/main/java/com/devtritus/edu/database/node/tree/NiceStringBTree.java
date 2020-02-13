package com.devtritus.edu.database.node.tree;

import java.nio.file.Path;
import java.util.*;

public class NiceStringBTree implements BTree<String, Long> {
    BTreeNode<String, Long> root;

    public NiceStringBTree(int m) {
        root = new BTreeNode<>(m, 0);
    }

    @Override
    public List<Long> search(String key) {
        return search(key, root);
    }

    @Override
    public String add(String key, Long value) {
        LinkedList<BTreeNode<String, Long>> path = new LinkedList<>();
        add(key, value, root, path);
        root = path.get(0);
        return key;
    }

    @Override
    public String delete(String key) {
        LinkedList<PathEntry> path = new LinkedList<>();
        delete(key, root, path, -1);
        root = path.get(0).key;
        return key;
    }

    private List<Long> search(String key, BTreeNode<String, Long> nextNode) {
        int index = nextNode.searchKey(key);
        if(index > -1) {
            Entry<String, Long> entry = nextNode.getKeyValue(index);
            return Collections.singletonList(entry.value);
        } else {
            if(nextNode.isLeaf()) {
                return Collections.emptyList();
            }

            BTreeNode<String, Long> child = nextNode.getChildNode(-index - 1);
            return search(key, child);
        }
    }

    private void add(String key, Long value, BTreeNode<String, Long> nextNode, LinkedList<BTreeNode<String, Long>> path) {
        path.add(nextNode);

        if(nextNode.isLeaf()) {
            doAdd(key, value, nextNode, path);
        } else {
            int index = nextNode.searchKey(key);
            if(index > -1) {
                nextNode.replaceValue(index, value);
            }
            BTreeNode<String, Long> nextChild = nextNode.getChildNode(-index - 1);

            add(key, value, nextChild, path);
        }
    }

    private void doAdd(String key, Long value, BTreeNode<String, Long> nextNode, LinkedList<BTreeNode<String, Long>> path) {
        nextNode.putKeyValue(key, value);

        if(nextNode.getKeysSize() < nextNode.m) {
            return;
        }

        BTreeNode<String, Long> nextParent;
        int insertionIndex;

        List<BTreeNode<String, Long>> parents = new ArrayList<>(path.subList(0, path.size() - 1));

        if(parents.isEmpty()) {
            nextParent = new BTreeNode<>(nextNode.m, nextNode.level + 1);
            insertionIndex = 0;
            path.addFirst(nextParent);
        } else {
            int lastParentIndex = parents.size() - 1;
            nextParent = parents.get(lastParentIndex);
            insertionIndex = nextParent.deleteChild(nextNode);
        }

        BTreeNode<String, Long> leftNode = nextNode.copy(0, nextNode.min);
        BTreeNode<String, Long> rightNode = nextNode.copy(nextNode.min + 1, nextNode.getKeysSize());

        nextParent.addChildNode(insertionIndex, leftNode);
        nextParent.addChildNode(insertionIndex + 1, rightNode);

        Entry<String, Long> middleKeyValue = nextNode.getKeyValue(nextNode.min);

        path.remove(path.size() - 1);

        doAdd(middleKeyValue.key, middleKeyValue.value, nextParent, path);
    }

    private void delete(String key, BTreeNode<String, Long> nextNode, LinkedList<PathEntry> path, int positionIndex) {
        PathEntry entry = new PathEntry(nextNode, positionIndex);
        path.add(entry);

        int index = nextNode.searchKey(key);

        if(index > -1) {
            doDelete(key, entry, path);
        } else if(nextNode.isLeaf()) {
            throw new IllegalStateException(String.format("Key %s not found", key));
        } else {
            int childPositionIndex = -index - 1;
            BTreeNode<String, Long> child = nextNode.getChildNode(childPositionIndex);
            delete(key, child, path, childPositionIndex);
        }
    }

    private void doDelete(String key, PathEntry pathEntry, LinkedList<PathEntry> path) {
        BTreeNode<String, Long> nextNode = pathEntry.key;
        int positionIndex = pathEntry.value;
        LinkedList<PathEntry> parents = new LinkedList<>(path.subList(0, path.size() - 1));

        if(nextNode.isLeaf()) {
            doDelete1(key, nextNode, path, positionIndex);
        } else {
            doDelete2(key, nextNode, path);
        }
    }

    private void doDelete1(String key, BTreeNode<String, Long> nextNode, LinkedList<PathEntry> path, int positionIndex) {
        int deletingKeyIndex = nextNode.searchKey(key);
        nextNode.deleteKeyValue(deletingKeyIndex);

        LinkedList<PathEntry> parents = new LinkedList<>(path.subList(0, path.size() - 1));

        if(nextNode.getKeysSize() < nextNode.min && !parents.isEmpty()) {
            PathEntry parentPathEntry = parents.get(parents.size() - 1);
            BTreeNode<String, Long> parentNode = parentPathEntry.key;

            BTreeNode<String, Long> leftNode = parentNode.getChildNode(positionIndex - 1);
            BTreeNode<String, Long> rightNode = parentNode.getChildNode(positionIndex + 1);

            if (rightNode != null && rightNode.getKeysSize() > rightNode.min) {
                move(nextNode, deletingKeyIndex, parentNode, positionIndex, rightNode, 0);
            } else if (leftNode != null && leftNode.getKeysSize() > leftNode.min) {
                move(nextNode, deletingKeyIndex, parentNode, positionIndex - 1, leftNode, leftNode.getKeysSize() - 1);
            } else if (rightNode != null) {
                BTreeNode<String, Long> unionNode = nextNode.union(rightNode);

                parentNode.deleteChild(nextNode);
                parentNode.deleteChild(rightNode);

                Entry<String, Long> parentKeyValue = parentNode.getKeyValue(positionIndex);
                unionNode.insertKeyValue(nextNode.getKeysSize(), parentKeyValue.key, parentKeyValue.value);

                parentNode.addChildNode(positionIndex, unionNode);

                path.remove(path.size() - 1);
                doDelete1(parentKeyValue.key, parentNode, path, parentPathEntry.value);

            } else if (leftNode != null) {
                BTreeNode<String, Long> unionNode = leftNode.union(nextNode);

                parentNode.deleteChild(leftNode);
                parentNode.deleteChild(nextNode);

                Entry<String, Long> parentKeyValue = parentNode.getKeyValue(positionIndex - 1);
                unionNode.insertKeyValue(leftNode.getKeysSize(), parentKeyValue.key, parentKeyValue.value);

                parentNode.addChildNode(positionIndex - 1, unionNode);

                path.remove(path.size() - 1);
                doDelete1(parentKeyValue.key, parentNode, path, parentPathEntry.value);

            } else {
                throw new IllegalStateException();
            }

            if(parents.isEmpty() && nextNode.getKeysSize() == 0 && nextNode.getChildrenSize() == 1) {
                path.remove(0);
                path.add(new PathEntry(nextNode.getChildNode(0), -1));
            }
        }
    }

    private void doDelete2(String key, BTreeNode<String, Long> nextNode, LinkedList<PathEntry> path) {
        int deletingKeyIndex = nextNode.searchKey(key);
        nextNode.deleteKeyValue(deletingKeyIndex);

        LinkedList<PathEntry> parents = new LinkedList<>(path.subList(0, path.size() - 1));

        if(parents.isEmpty() && nextNode.getKeysSize() == 0 && nextNode.getChildrenSize() == 1) {
            path.remove(0);
            path.add(new PathEntry(nextNode.getChildNode(0), -1));
        } else if(nextNode.getKeysSize() < nextNode.min || nextNode.getKeysSize() != nextNode.getChildrenSize() - 1) {
            BTreeNode<String, Long> leftChildNode = nextNode.getChildNode(deletingKeyIndex);
            BTreeNode<String, Long> rightChildNode = nextNode.getChildNode(deletingKeyIndex + 1);

            if (rightChildNode != null) {
                Entry<String, Long> entry = getMinKey(rightChildNode, path, deletingKeyIndex + 1);
                nextNode.insertKeyValue(deletingKeyIndex, entry.key, entry.value);
                doDelete(entry.key, path.get(path.size() - 1), path);

            } else if (leftChildNode != null) {
                Entry<String, Long> entry = getMaxKey(leftChildNode, path, deletingKeyIndex);
                nextNode.insertKeyValue(deletingKeyIndex, entry.key, entry.value);
                doDelete(entry.key, path.get(path.size() - 1), path);

            } else {
                throw new IllegalStateException();
            }
        }
    }

    private static void move(BTreeNode<String, Long> currentNode, int deletingKeyIndex, BTreeNode<String, Long> parentNode,
            int parentKeyIndex, BTreeNode<String, Long> donatingNode,  int donatingKeyIndex) {

        Entry<String, Long> parentKeyValue = parentNode.deleteKeyValue(parentKeyIndex);
        currentNode.insertKeyValue(deletingKeyIndex, parentKeyValue.key, parentKeyValue.value);
        Entry<String, Long> donatingNodeKeyValue = donatingNode.deleteKeyValue(donatingKeyIndex);
        parentNode.insertKeyValue(parentKeyIndex, donatingNodeKeyValue.key, donatingNodeKeyValue.value);
    }

    private static Entry<String, Long> getMaxKey(BTreeNode<String, Long> nextNode, LinkedList<PathEntry> path, int positionIndex) {
        path.add(new PathEntry(nextNode, positionIndex));

        int maxKeyIndex = nextNode.getKeysSize() - 1;
        if(nextNode.isLeaf()) {
            return nextNode.getKeyValue(maxKeyIndex);
        } else {
            return getMaxKey(nextNode.getChildNode(maxKeyIndex + 1), path, maxKeyIndex + 1);
        }
    }

    private static Entry<String, Long> getMinKey(BTreeNode<String, Long> nextNode, LinkedList<PathEntry> path, int positionIndex) {
        path.add(new PathEntry(nextNode, positionIndex));

        int minKeyIndex = 0;
        if(nextNode.isLeaf()) {
            return nextNode.getKeyValue(minKeyIndex);
        } else {
            return getMinKey(nextNode.getChildNode(minKeyIndex), path, minKeyIndex);
        }
    }
}
