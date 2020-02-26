package com.devtritus.edu.database.node.tree;

import java.util.*;

abstract class AbstractBTree<K extends Comparable<K>, V> implements BTree<K, V> {
    private static final int ROOT_POSITION = -1;

    BTreeNode<K, V> root;

    private final int m;
    private final int min;

    AbstractBTree(int m) {
        if(m < 3) {
            throw new IllegalArgumentException("m must be more or equal then 3");
        }

        this.m = m;

        //m = 3 => 3/2 = 1.5 ~ 2 => t = 2 => t - 1 = 2 - 1
        //m = 4 => 4/2 =   2 ~ 2 => t = 2 => t - 1 = 2 - 1
        this.min = (int)Math.ceil(m / 2d) - 1;

        root = new BTreeNode<>(0);
    }

    abstract boolean isFetchedKeySatisfy(K key, K fetchKey);

    @Override
    public Map<K, V> fetch(K key) {
        Map<K, V> result = new HashMap<>();
        fetch(key, root, result);
        return result;
    }

    @Override
    public V searchByKey(K key) {
        return searchByKey(key, root);
    }

    @Override
    public K add(K key, V value) {
        LinkedList<BTreeNode<K, V>> path = new LinkedList<>();
        add(key, value, root, path);
        root = path.get(0);
        return key;
    }

    @Override
    public boolean delete(K key) {
        LinkedList<PathEntry<K, V>> path = new LinkedList<>();
        boolean result = delete(key, root, path, ROOT_POSITION, null);
        root = path.get(0).key;

        return result;
    }

    @Override
    public boolean isEmpty() {
        if(getKeysSize(root) == 0 && getChildrenSize(root) != 0)  {
            throw new IllegalStateException();
        }
        return getKeysSize(root) == 0;
    }

    private void fetch(K key, BTreeNode<K, V> nextNode, Map<K, V> result) {

        int lastChildIndex = 0;
        boolean found = false;
        List<K> keys = nextNode.getKeys();
        for (int i = 0; i < keys.size(); i++) {
            Entry<K, V> entry = nextNode.getKeyValue(i);
            if (isFetchedKeySatisfy(entry.key, key)) {
                found = true;
                result.put(entry.key, entry.value);

                if (!isLeaf(nextNode)) {
                    if (lastChildIndex == 0 || lastChildIndex < i) {
                        BTreeNode<K, V> leftNode = nextNode.getChildNode(i);
                        if (leftNode != null) {
                            fetch(key, leftNode, result);
                        }
                    }

                    BTreeNode<K, V> rightNode = nextNode.getChildNode(i + 1);
                    if (rightNode != null) {
                        fetch(key, rightNode, result);
                        lastChildIndex = i + 1;
                    }
                }
            } else if(found) {
                break;
            }
        }
        if (!found) {
            int index = searchKey(nextNode, key);
            if (index > -1) {
                throw new IllegalStateException();
            } else if (!isLeaf(nextNode)) {
                BTreeNode<K, V> child = nextNode.getChildNode(-index - 1);
                fetch(key, child, result);
            }
        }
    }

    private V searchByKey(K key, BTreeNode<K, V> nextNode) {
        int index = searchKey(nextNode, key);
        if(index > -1) {
            return nextNode.getKeyValue(index).value;
        } else {
            if(isLeaf(nextNode)) {
                return null;
            }

            BTreeNode<K, V> child = nextNode.getChildNode(-index - 1);
            return searchByKey(key, child);
        }
    }

    private void add(K key, V value, BTreeNode<K, V> nextNode, LinkedList<BTreeNode<K, V>> path) {
        path.add(nextNode);

        if(isLeaf(nextNode)) {
            doAdd(key, value, nextNode, path);
        } else {
            int index = searchKey(nextNode, key);
            if(index > -1) {
                nextNode.putKeyValue(index, key, value);
            }
            BTreeNode<K, V> nextChild = nextNode.getChildNode(-index - 1);

            add(key, value, nextChild, path);
        }
    }

    private void doAdd(K key, V value, BTreeNode<K, V> nextNode, LinkedList<BTreeNode<K, V>> path) {
        int index = searchKey(nextNode, key);
        nextNode.putKeyValue(index, key, value);

        if(getKeysSize(nextNode) >= m) {
            balanceAfterAdd(nextNode, path);
        }
    }

    private void balanceAfterAdd(BTreeNode<K, V> nextNode, LinkedList<BTreeNode<K, V>> path) {
        BTreeNode<K, V> nextParent;
        int insertionIndex;

        List<BTreeNode<K, V>> parents = new ArrayList<>(path.subList(0, path.size() - 1));

        if(parents.isEmpty()) {
            nextParent = new BTreeNode<>(nextNode.getLevel() + 1);
            path.addFirst(nextParent);
            nextParent.addChildNode(0, nextNode);
            insertionIndex = 0;
        } else {
            int lastParentIndex = parents.size() - 1;
            nextParent = parents.get(lastParentIndex);
            insertionIndex = indexOfChild(nextParent, nextNode);
        }

        BTreeNode<K, V> rightNode = new BTreeNode<>(nextNode.getLevel());
        rightNode.copy(min + 1, getKeysSize(nextNode), nextNode);
        nextNode.deleteInterval(min + 1, getKeysSize(nextNode));

        nextParent.addChildNode(insertionIndex + 1, rightNode);

        Entry<K, V> middleKeyValue = nextNode.deleteKeyValue(min);

        path.remove(path.size() - 1);

        doAdd(middleKeyValue.key, middleKeyValue.value, nextParent, path);
    }

    private boolean delete(K key, BTreeNode<K, V> nextNode, LinkedList<PathEntry<K, V>> path, int positionIndex, BTreeNode<K, V> parent) {
        PathEntry<K, V> entry = new PathEntry<>(nextNode, positionIndex);
        path.add(entry);

        int index = searchKey(nextNode, key);

        if(index > -1) {
            if(isLeaf(nextNode)) {
                nextNode.deleteKeyValue(index);

                if(getKeysSize(nextNode) < min && parent != null) {
                    balanceAfterDelete(nextNode, path, positionIndex, parent);
                }
            } else if(!isLeaf(nextNode)) {
                deleteInternalNode(index, nextNode, path);
            }
            return true;
        } else if(isLeaf(nextNode)) {
            return false;
        } else {
            int childPositionIndex = -index - 1;
            BTreeNode<K, V> child = nextNode.getChildNode(childPositionIndex);
            return delete(key, child, path, childPositionIndex, nextNode);
        }
    }

    private void deleteInternalNode(int deletingKeyIndex, BTreeNode<K, V> nextNode, LinkedList<PathEntry<K, V>> path) {
        BTreeNode<K, V> rightChildNode = nextNode.getChildNode(deletingKeyIndex + 1);
        BTreeNode<K, V> leftChildNode = nextNode.getChildNode(deletingKeyIndex);

        if(rightChildNode != null) {
            Entry<K, V> entry = getMinKey(rightChildNode, path, deletingKeyIndex + 1);
            PathEntry<K, V> rightMinNodeEntry = path.get(path.size() - 1);
            PathEntry<K, V> rightMinNodeParentEntry = path.get(path.size() - 2);
            replaceAfterDelete(nextNode, rightMinNodeEntry.key, rightMinNodeParentEntry.key, entry, path, rightMinNodeEntry.value, deletingKeyIndex, 0);

        } else if(leftChildNode != null) {
            Entry<K, V> entry = getMaxKey(leftChildNode, path, deletingKeyIndex);
            PathEntry<K, V> leftMaxNodeEntry = path.get(path.size() - 1);
            PathEntry<K, V> leftMaxNodeParentEntry = path.get(path.size() - 1);
            replaceAfterDelete(nextNode, leftMaxNodeEntry.key, leftMaxNodeParentEntry.key, entry, path, leftMaxNodeEntry.value, deletingKeyIndex, getKeysSize(leftMaxNodeEntry.key));

        } else {
            throw new IllegalStateException();
        }
    }

    private void replaceAfterDelete(BTreeNode<K, V> consumerNode,
                                    BTreeNode<K, V> sourceNode,
                                    BTreeNode<K, V> sourceNodeParent,
                                    Entry<K, V> entryToReplace,
                                    LinkedList<PathEntry<K, V>> path,
                                    int sourcePositionIndex,
                                    int consumerKeyIndex,
                                    int sourceKeyIndex) {

        sourceNode.deleteKeyValue(sourceKeyIndex);
        consumerNode.deleteKeyValue(consumerKeyIndex);
        consumerNode.insertKeyValue(consumerKeyIndex, entryToReplace.key, entryToReplace.value);

        if(getKeysSize(sourceNode) < min) {
            balanceAfterDelete(sourceNode, path, sourcePositionIndex, sourceNodeParent);
        }
    }

    private void balanceAfterDelete(BTreeNode<K, V> nextNode, LinkedList<PathEntry<K, V>> path, int positionIndex, BTreeNode<K, V> parentNode) {
        BTreeNode<K, V> leftNode = parentNode.getChildNode(positionIndex - 1);
        BTreeNode<K, V> rightNode = parentNode.getChildNode(positionIndex + 1);

        if(rightNode != null && getKeysSize(rightNode) > min) {
            rotate(nextNode, parentNode, rightNode, getKeysSize(nextNode), positionIndex, 0, 0, getChildrenSize(nextNode));

        } else if(leftNode != null && getKeysSize(leftNode) > min) {
            rotate(nextNode, parentNode, leftNode, 0, positionIndex - 1, getKeysSize(leftNode) - 1, getChildrenSize(leftNode) - 1, 0);

        } else if(rightNode != null) {
            union(nextNode, parentNode, rightNode, path, positionIndex);

        } else if(leftNode != null) {
            union(leftNode, parentNode, nextNode, path, positionIndex - 1);
        }
    }

    private void rotate(BTreeNode<K, V> consumerNode,
                        BTreeNode<K, V> parentNode,
                        BTreeNode<K, V> sourceNode,
                        int consumerKeyIndex,
                        int parentKeyIndex,
                        int sourceKeyIndex,
                        int sourceChildIndex,
                        int consumerChildInsertIndex) {

        Entry<K, V> parentKeyValue = parentNode.deleteKeyValue(parentKeyIndex);
        consumerNode.insertKeyValue(consumerKeyIndex, parentKeyValue.key, parentKeyValue.value);
        Entry<K, V> sourceKeyValue = sourceNode.deleteKeyValue(sourceKeyIndex);
        parentNode.insertKeyValue(parentKeyIndex, sourceKeyValue.key, sourceKeyValue.value);

        if(getChildrenSize(sourceNode) > 0) {
            BTreeNode<K, V> deletedChild = sourceNode.deleteChild(sourceChildIndex);
            consumerNode.addChildNode(consumerChildInsertIndex, deletedChild);
        }
    }

    private void union(BTreeNode<K, V> firstNode,
                       BTreeNode<K, V> parentNode,
                       BTreeNode<K, V> secondNode,
                       LinkedList<PathEntry<K, V>> path,
                       int parentKeyIndex) {

        Entry<K, V> parentKeyValue = parentNode.getKeyValue(parentKeyIndex);
        firstNode.insertKeyValue(getKeysSize(firstNode), parentKeyValue.key, parentKeyValue.value);

        firstNode.add(secondNode);

        int secondNodeIndex = indexOfChild(parentNode, secondNode);
        parentNode.deleteChild(secondNodeIndex);

        path.remove(path.size() - 1);

        parentNode.deleteKeyValue(parentKeyIndex);

        if(parentNode == path.get(0).key) {
            if(getChildrenSize(parentNode) > 0 && getKeysSize(parentNode) == 0) {
                path.remove(0);
                path.add(new PathEntry<>(firstNode, 0));
            }
        } else if(getKeysSize(parentNode) < min) {
            int parentPositionIndex = path.get(path.size() - 1).value;
            balanceAfterDelete(parentNode, path, parentPositionIndex, path.get(path.size() - 2).key);
        }
    }

    private Entry<K, V> getMaxKey(BTreeNode<K, V> nextNode,
                                  LinkedList<PathEntry<K, V>> path,
                                  int positionIndex) {

        path.add(new PathEntry<>(nextNode, positionIndex));

        int maxKeyIndex = getKeysSize(nextNode) - 1;
        if(isLeaf(nextNode)) {
            return nextNode.getKeyValue(maxKeyIndex);
        } else {
            return getMaxKey(nextNode.getChildNode(maxKeyIndex + 1), path, maxKeyIndex + 1);
        }
    }

    private Entry<K, V> getMinKey(BTreeNode<K, V> nextNode,
                                  LinkedList<PathEntry<K, V>> path,
                                  int positionIndex) {

        path.add(new PathEntry<>(nextNode, positionIndex));

        int minKeyIndex = 0;
        if(isLeaf(nextNode)) {
            return nextNode.getKeyValue(minKeyIndex);
        } else {
            return getMinKey(nextNode.getChildNode(minKeyIndex), path, minKeyIndex);
        }
    }

    private int getKeysSize(BTreeNode<K, V> node) {
        return node.getKeys().size();
    }

    private int getChildrenSize(BTreeNode<K, V> node) {
        return node.getChildren().size();
    }

    private int searchKey(BTreeNode<K, V> node, K key) {
        return node.searchKey(key);
    }

    private int indexOfChild(BTreeNode<K, V> parent, BTreeNode<K, V> child) {
        return parent.getChildren().indexOf(child);
    }

    private boolean isLeaf(BTreeNode<K, V> node) {
        return node.getLevel() == 0;
    }
}
