package com.devtritus.edu.database.node.tree;

import java.util.*;

public class StringBTree implements BTree<String, Long> {
    private static final int ROOT_POSITION = -99999;

    BTreeNode<String, Long> root;

    public StringBTree(int m) {
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

    @Override
    public boolean isEmpty() {
        if(root.getKeysSize() == 0 && root.getChildrenSize() != 0)  {
            throw new IllegalStateException();
        }
        return root.getKeysSize() == 0;
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
            if(nextNode.isLeaf()) {
                nextNode.deleteKeyValue(index);

                if(nextNode.getKeysSize() < nextNode.min && parent != null) {
                    rebalance(nextNode, path, positionIndex, parent);
                }
            } else if(!nextNode.isLeaf()) {
                deleteInternalNode(index, nextNode, path);
            } else {
                throw new IllegalStateException();
            }
        } else if(nextNode.isLeaf()) {
            throw new IllegalStateException(String.format("Key %s not found", key));
        } else {
            int childPositionIndex = -index - 1;
            BTreeNode<String, Long> child = nextNode.getChildNode(childPositionIndex);
            delete(key, child, path, childPositionIndex, nextNode);
        }
    }

    private void rebalance(BTreeNode<String, Long> nextNode, LinkedList<PathEntry> path, int positionIndex, BTreeNode<String, Long> parentNode) {
        BTreeNode<String, Long> leftNode = parentNode.getChildNode(positionIndex - 1);
        BTreeNode<String, Long> rightNode = parentNode.getChildNode(positionIndex + 1);

        if(rightNode != null && rightNode.getKeysSize() > rightNode.min) {
            rotate(nextNode, parentNode, rightNode, nextNode.getKeysSize(), positionIndex, 0, 0, nextNode.getChildrenSize());

        } else if(leftNode != null && leftNode.getKeysSize() > leftNode.min) {
            rotate(nextNode, parentNode, leftNode, 0, positionIndex - 1, leftNode.getKeysSize() - 1, leftNode.getChildrenSize() - 1, 0);

        } else if(rightNode != null) {
            union(nextNode, parentNode, rightNode, path, positionIndex);

        } else if(leftNode != null) {
            union(leftNode, parentNode, nextNode, path, positionIndex - 1);
        }
    }

    private void rotate(BTreeNode<String, Long> consumerNode,
                        BTreeNode<String, Long> parentNode,
                        BTreeNode<String, Long> sourceNode,
                        int consumerKeyIndex,
                        int parentKeyIndex,
                        int sourceKeyIndex,
                        int sourceChildIndex,
                        int consumerChildInsertIndex) {

        Entry<String, Long> parentKeyValue = parentNode.deleteKeyValue(parentKeyIndex);
        consumerNode.insertKeyValue(consumerKeyIndex, parentKeyValue.key, parentKeyValue.value);
        Entry<String, Long> sourceKeyValue = sourceNode.deleteKeyValue(sourceKeyIndex);
        parentNode.insertKeyValue(parentKeyIndex, sourceKeyValue.key, sourceKeyValue.value);

        if(sourceNode.getChildrenSize() > 0) {
            BTreeNode<String, Long> deletedChild = sourceNode.deleteChild(sourceChildIndex);
            consumerNode.addChildNode(consumerChildInsertIndex, deletedChild);
        }
    }

    private void union(BTreeNode<String, Long> firstNode,
                       BTreeNode<String, Long> parentNode,
                       BTreeNode<String, Long> secondNode,
                       LinkedList<PathEntry> path,
                       int parentKeyIndex) {

        BTreeNode<String, Long> unionNode = firstNode.union(secondNode);

        parentNode.deleteChild(firstNode);
        parentNode.deleteChild(secondNode);

        Entry<String, Long> parentKeyValue = parentNode.getKeyValue(parentKeyIndex);
        unionNode.insertKeyValue(firstNode.getKeysSize(), parentKeyValue.key, parentKeyValue.value);

        parentNode.addChildNode(parentKeyIndex, unionNode);

        path.remove(path.size() - 1);

        parentNode.deleteKeyValue(parentKeyIndex);

        if(parentNode == path.get(0).key) {
            if(parentNode.getChildrenSize() > 0 && parentNode.getKeysSize() == 0) {
                path.remove(0);
                path.add(new PathEntry(unionNode, 0));
            }
        } else if(parentNode.getKeysSize() < parentNode.min) {
            int parentPositionIndex = path.get(path.size() - 1).value;
            rebalance(parentNode, path, parentPositionIndex, path.get(path.size() - 2).key);
        }
    }

    private void deleteInternalNode(int deletingKeyIndex, BTreeNode<String, Long> nextNode, LinkedList<PathEntry> path) {
        BTreeNode<String, Long> rightChildNode = nextNode.getChildNode(deletingKeyIndex + 1);
        BTreeNode<String, Long> leftChildNode = nextNode.getChildNode(deletingKeyIndex);

        if(rightChildNode != null) {
            Entry<String, Long> entry = getMinKey(rightChildNode, path, deletingKeyIndex + 1);
            PathEntry rightMinNodeEntry = path.get(path.size() - 1);
            rightMinNodeEntry.key.deleteKeyValue(0);

            nextNode.replaceKeyValue(deletingKeyIndex, entry.key, entry.value);
            if(rightMinNodeEntry.key.getKeysSize() < rightMinNodeEntry.key.min) {
                rebalance(rightMinNodeEntry.key, path, rightMinNodeEntry.value, path.get(path.size() - 2).key);
            }

        } else if(leftChildNode != null) {
            Entry<String, Long> entry = getMaxKey(leftChildNode, path, deletingKeyIndex);
            PathEntry leftMaxNodeEntry = path.get(path.size() - 1);
            leftMaxNodeEntry.key.deleteKeyValue(leftMaxNodeEntry.key.getKeysSize() - 1);

            nextNode.replaceKeyValue(deletingKeyIndex, entry.key, entry.value);

            if(leftMaxNodeEntry.key.getKeysSize() < leftMaxNodeEntry.key.min) {
                rebalance(leftMaxNodeEntry.key, path, leftMaxNodeEntry.value, path.get(path.size() - 2).key);
            }

        } else {
            throw new IllegalStateException();
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
