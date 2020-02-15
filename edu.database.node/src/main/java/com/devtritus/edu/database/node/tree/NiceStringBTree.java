package com.devtritus.edu.database.node.tree;

import java.util.*;

public class NiceStringBTree implements BTree<String, Long> {
    private static final int ROOT_POSITION = -99999;

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
        delete(key, root, path, ROOT_POSITION, null);
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

    private void delete(String key, BTreeNode<String, Long> nextNode, LinkedList<PathEntry> path, int positionIndex, BTreeNode<String, Long> parent) {
        PathEntry entry = new PathEntry(nextNode, positionIndex);
        path.add(entry);

        int index = nextNode.searchKey(key);

        if(index > -1) {
            doDelete(key, index, entry, path, parent);
        } else if(nextNode.isLeaf()) {
            throw new IllegalStateException(String.format("Key %s not found", key));
        } else {
            int childPositionIndex = -index - 1;
            BTreeNode<String, Long> child = nextNode.getChildNode(childPositionIndex);
            delete(key, child, path, childPositionIndex, nextNode);
        }
    }

    private void doDelete(String key, int keyIndex, PathEntry pathEntry, LinkedList<PathEntry> path, BTreeNode<String, Long> parent) {
        BTreeNode<String, Long> nextNode = pathEntry.key;
        int positionIndex = pathEntry.value;
        doDelete(key, keyIndex, nextNode, path, positionIndex, parent);
    }

    private void doDelete(String key, int deletingKeyIndex, BTreeNode<String, Long> nextNode, LinkedList<PathEntry> path, int positionIndex, BTreeNode<String, Long> parentNode) {
        if(path.size() == 1 && nextNode.getChildrenSize() == 0) {
            nextNode.deleteKeyValue(deletingKeyIndex);
        } else if(nextNode.isLeaf()) {
            nextNode.deleteKeyValue(deletingKeyIndex);

            if(nextNode.getKeysSize() < nextNode.min) {
                balanceAsLeaf(deletingKeyIndex, nextNode, path, positionIndex, parentNode);
            }
        } else if(!nextNode.isLeaf()) {
            balanceAsInnerNode(deletingKeyIndex, nextNode, path, positionIndex, parentNode);
        } else {
            throw new IllegalStateException();
        }
    }


    private void balanceAsLeaf(int deletingKeyIndex, BTreeNode<String, Long> nextNode, LinkedList<PathEntry> path, int positionIndex, BTreeNode<String, Long> parentNode) {
        BTreeNode<String, Long> leftNode = parentNode.getChildNode(positionIndex - 1);
        BTreeNode<String, Long> rightNode = parentNode.getChildNode(positionIndex + 1);

        if (rightNode != null && rightNode.getKeysSize() > rightNode.min) {

            Entry<String, Long> parentKeyValue = parentNode.deleteKeyValue(positionIndex);
            nextNode.insertKeyValue(deletingKeyIndex, parentKeyValue.key, parentKeyValue.value);
            Entry<String, Long> donatingNodeKeyValue = rightNode.deleteKeyValue(positionIndex + 1);
            parentNode.insertKeyValue(positionIndex, donatingNodeKeyValue.key, donatingNodeKeyValue.value);

        } else if (leftNode != null && leftNode.getKeysSize() > leftNode.min) {

            Entry<String, Long> parentKeyValue = parentNode.deleteKeyValue(positionIndex - 1);
            nextNode.insertKeyValue(deletingKeyIndex, parentKeyValue.key, parentKeyValue.value);
            Entry<String, Long> donatingNodeKeyValue = leftNode.deleteKeyValue(positionIndex - 1);
            parentNode.insertKeyValue(positionIndex - 1, donatingNodeKeyValue.key, donatingNodeKeyValue.value);

        } else if (rightNode != null) {
            BTreeNode<String, Long> unionNode = nextNode.union(rightNode);

            parentNode.deleteChild(nextNode);
            parentNode.deleteChild(rightNode);

            Entry<String, Long> parentKeyValue = parentNode.getKeyValue(positionIndex);
            unionNode.insertKeyValue(nextNode.getKeysSize(), parentKeyValue.key, parentKeyValue.value);

            parentNode.addChildNode(positionIndex, unionNode);

            path.remove(path.size() - 1);
            parentNode.deleteKeyValue(positionIndex + 1);

            if(path.size() == 1 && parentNode.getKeysSize() == 0) {
                path.remove(0);
            } else if(parentNode.getKeysSize() < parentNode.m) {
                int parentPositionIndex = path.get(path.size() - 1).value;
                balanceAsLeaf(positionIndex + 1, parentNode, path, parentPositionIndex, path.get(path.size() - 2).key);
            }

        } else if(leftNode != null) {
            BTreeNode<String, Long> unionNode = leftNode.union(nextNode);

            parentNode.deleteChild(leftNode);
            parentNode.deleteChild(nextNode);

            Entry<String, Long> parentKeyValue = parentNode.getKeyValue(positionIndex - 1);
            unionNode.insertKeyValue(leftNode.getKeysSize(), parentKeyValue.key, parentKeyValue.value);

            parentNode.addChildNode(positionIndex - 1, unionNode);

            path.remove(path.size() - 1);
            parentNode.deleteKeyValue(positionIndex - 1);

            if(path.size() == 1 && parentNode.getKeysSize() == 0) {
                path.remove(0);
            } else if(parentNode.getKeysSize() < parentNode.m) {
                int parentPositionIndex = path.get(path.size() - 1).value;
                balanceAsLeaf(positionIndex - 1, parentNode, path, parentPositionIndex, path.get(path.size() - 2).key);
            }
        }
    }

    private void balanceAsInnerNode(int deletingKeyIndex, BTreeNode<String, Long> nextNode, LinkedList<PathEntry> path, int positionIndex, BTreeNode<String, Long> parentNode) {
        Entry<String, Long> nextNodeEntry = nextNode.getKeyValue(deletingKeyIndex);
        BTreeNode<String, Long> rightChildNode = nextNode.getChildNode(deletingKeyIndex + 1);
        BTreeNode<String, Long> leftChildNode = nextNode.getChildNode(deletingKeyIndex);
        BTreeNode<String, Long> root = path.get(0).key;

        if (rightChildNode != null) {
            Entry<String, Long> entry = getMinKey(rightChildNode, path, deletingKeyIndex + 1);
            PathEntry rightMinNodeEntry = path.get(path.size() - 1);
            int rightMinKeyIndex = rightMinNodeEntry.key.searchKey(entry.key);
            rightChildNode.deleteKeyValue(rightMinKeyIndex);
            balanceAsLeaf(rightMinKeyIndex, rightMinNodeEntry.key, path, rightMinNodeEntry.value, path.get(path.size() - 2).key);
            replace(nextNodeEntry.key, entry.key, entry.value, root);

        } else if (leftChildNode != null) {
            Entry<String, Long> entry = getMaxKey(leftChildNode, path, deletingKeyIndex);
            PathEntry leftMinNodeEntry = path.get(path.size() - 1);
            int leftMinKeyIndex = leftMinNodeEntry.key.searchKey(entry.key);
            leftChildNode.deleteKeyValue(leftMinKeyIndex);
            balanceAsLeaf(leftMinKeyIndex, leftMinNodeEntry.key, path, leftMinNodeEntry.value, path.get(path.size() - 2).key);
            replace(nextNodeEntry.key, entry.key, entry.value, root);

        } else {
            throw new IllegalStateException();
        }
    }

    private static void replace(String key, String newKey, long newValue, BTreeNode<String, Long> nextNode) {
        int index = nextNode.searchKey(key);
        if(index > -1) {
            nextNode.replaceKeyValue(index, newKey, newValue);
        } else {
            if(nextNode.isLeaf()) {
                throw new IllegalStateException(String.format("Key %s not found", key));
            }

            BTreeNode<String, Long> child = nextNode.getChildNode(-index - 1);
            replace(key, newKey, newValue, child);
        }
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
