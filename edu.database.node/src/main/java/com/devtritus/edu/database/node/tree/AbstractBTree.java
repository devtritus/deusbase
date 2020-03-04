package com.devtritus.edu.database.node.tree;

import java.util.*;

abstract class AbstractBTree<D extends GenericBTreeNode<K, V, C>, K extends Comparable<K>, V, C> implements BTree<K, V> {
    private BTreeNodeProvider<D, K, V, C> nodeProvider;

    private final int m;
    private final int min;

    AbstractBTree(int m, BTreeNodeProvider<D, K, V, C> nodeProvider) {
        if(m < 3) {
            throw new IllegalArgumentException("m must be more or equal then 3");
        }

        this.m = m;
        this.nodeProvider = nodeProvider;

        //m = 3 => 3/2 = 1.5 ~ 2 => t = 2 => t - 1 = 2 - 1
        //m = 4 => 4/2 =   2 ~ 2 => t = 2 => t - 1 = 2 - 1
        this.min = (int)Math.ceil(m / 2d) - 1;
    }

    abstract boolean isFetchedKeySatisfy(K key, K fetchKey);

    @Override
    public Map<K, V> fetch(K key) {
        Map<K, V> result = new HashMap<>();
        PathEntry<D, K, V, C> root = nodeProvider.getRootNode();
        fetch(key, root.key, result);
        return result;
    }

    @Override
    public V searchByKey(K key) {
        PathEntry<D, K, V, C> root = nodeProvider.getRootNode();
        return searchByKey(key, root.key);
    }

    @Override
    public K add(K key, V value) {
        LinkedList<PathEntry<D, K, V, C>> path = new LinkedList<>();
        PathEntry<D, K, V, C> root = nodeProvider.getRootNode();
        add(key, value, root.key, path, root.value);

        nodeProvider.setRootNode(path.get(0));
        nodeProvider.flush();

        return key;
    }

    @Override
    public boolean delete(K key) {
        LinkedList<PathEntry<D, K, V, C>> path = new LinkedList<>();
        PathEntry<D, K, V, C> root = nodeProvider.getRootNode();
        boolean result = delete(key, root.key, path, root.value, null);

        nodeProvider.setRootNode(path.get(0));
        nodeProvider.flush();

        return result;
    }

    @Override
    public boolean isEmpty() {
        PathEntry<D, K, V, C> root = nodeProvider.getRootNode();
        if(root.key.getKeysSize() == 0 && root.key.getChildrenSize() != 0)  {
            throw new IllegalStateException();
        }
        return root.key.getKeysSize() == 0;
    }

    private void fetch(K key, D nextNode, Map<K, V> result) {

        int lastChildIndex = 0;
        boolean found = false;
        List<K> keys = nextNode.getKeys();
        for (int i = 0; i < keys.size(); i++) {
            Entry<K, V> entry = nextNode.getKeyValue(i);
            if (isFetchedKeySatisfy(entry.key, key)) {
                found = true;
                result.put(entry.key, entry.value);

                if (!nextNode.isLeaf()) {
                    if (lastChildIndex == 0 || lastChildIndex < i) {
                        D leftNode = nodeProvider.getChildNode(nextNode, i);
                        if (leftNode != null) {
                            fetch(key, leftNode, result);
                        }
                    }

                    D rightNode = nodeProvider.getChildNode(nextNode, i + 1);
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
            int index = nextNode.searchKey(key);
            if (index > -1) {
                throw new IllegalStateException();
            } else if (!nextNode.isLeaf()) {
                D child = nodeProvider.getChildNode(nextNode, -index - 1);
                fetch(key, child, result);
            }
        }
    }

    private V searchByKey(K key, D nextNode) {
        int index = nextNode.searchKey(key);
        if(index > -1) {
            return nextNode.getKeyValue(index).value;
        } else {
            if(nextNode.isLeaf()) {
                return null;
            }

            D child = nodeProvider.getChildNode(nextNode, -index - 1);
            return searchByKey(key, child);
        }
    }

    private void add(K key, V value, D nextNode, LinkedList<PathEntry<D, K, V, C>> path, int positionIndex) {
        path.add(new PathEntry<>(nextNode, positionIndex));

        if(nextNode.isLeaf()) {
            doAdd(key, value, nextNode, path);
        } else {
            int index = nextNode.searchKey(key);
            if(index > -1) {
                nextNode.putKeyValue(index, key, value);
                return; //TODO: resolve a problem with matched keys
            }
            int childPositionIndex = -index - 1;
            D nextChild = nodeProvider.getChildNode(nextNode, childPositionIndex);

            add(key, value, nextChild, path, childPositionIndex);
        }
    }

    private void doAdd(K key, V value, D nextNode, LinkedList<PathEntry<D, K, V, C>> path) {
        int index = nextNode.searchKey(key);
        nextNode.putKeyValue(index, key, value);

        if(nextNode.getKeysSize() >= m) {
            balanceAfterAdd(nextNode, path);
        }
    }

    private void balanceAfterAdd(D nextNode, LinkedList<PathEntry<D, K, V, C>> path) {
        PathEntry<D, K, V, C> nextParent;
        int insertionIndex;

        List<PathEntry<D, K, V, C>> parents = new ArrayList<>(path.subList(0, path.size() - 1));

        if(parents.isEmpty()) {
            nextParent = nodeProvider.createNode(nextNode.getLevel() + 1);
            path.addFirst(nextParent);
            nodeProvider.insertChildNode(nextParent.key, path.get(path.size() - 1), 0); //TODO: pass nextNode only in path list
            insertionIndex = 0;
        } else {
            nextParent = parents.get(parents.size() - 1);
            insertionIndex = path.get(path.size() - 1).value;
        }

        PathEntry<D, K, V, C> rightNode = nodeProvider.createNode(nextNode.getLevel());
        rightNode.key.copy(nextNode, min + 1, nextNode.getKeysSize());
        nextNode.delete(min + 1, nextNode.getKeysSize());

        nodeProvider.insertChildNode(nextParent.key, rightNode, insertionIndex + 1);

        Entry<K, V> middleKeyValue = nextNode.deleteKeyValue(min);

        path.remove(path.size() - 1);

        doAdd(middleKeyValue.key, middleKeyValue.value, nextParent.key, path);
    }

    private boolean delete(K key, D nextNode, LinkedList<PathEntry<D, K, V, C>> path, int positionIndex, D parent) {
        PathEntry<D, K, V, C> entry = new PathEntry<>(nextNode, positionIndex);
        path.add(entry);

        int index = nextNode.searchKey(key);

        if(index > -1) {
            if(nextNode.isLeaf()) {
                nextNode.deleteKeyValue(index);

                if(nextNode.getKeysSize() < min && parent != null) {
                    balanceAfterDelete(nextNode, path, positionIndex, parent);
                }
            } else if(!nextNode.isLeaf()) {
                deleteInternalNode(index, nextNode, path);
            }
            return true;
        } else if(nextNode.isLeaf()) {
            return false;
        } else {
            int childPositionIndex = -index - 1;
            D child = nodeProvider.getChildNode(nextNode, childPositionIndex);
            return delete(key, child, path, childPositionIndex, nextNode);
        }
    }

    private void deleteInternalNode(int deletingKeyIndex, D nextNode, LinkedList<PathEntry<D, K, V, C>> path) {
        D rightChildNode = nodeProvider.getChildNode(nextNode, deletingKeyIndex + 1);
        D leftChildNode = nodeProvider.getChildNode(nextNode, deletingKeyIndex);

        if(rightChildNode != null) {
            Entry<K, V> entry = getMinKey(rightChildNode, path, deletingKeyIndex + 1);
            PathEntry<D, K, V, C> rightMinNodeEntry = path.get(path.size() - 1);
            PathEntry<D, K, V, C> rightMinNodeParentEntry = path.get(path.size() - 2);
            replaceAfterDelete(nextNode, rightMinNodeEntry.key, rightMinNodeParentEntry.key, entry, path, rightMinNodeEntry.value, deletingKeyIndex, 0);

        } else if(leftChildNode != null) {
            Entry<K, V> entry = getMaxKey(leftChildNode, path, deletingKeyIndex);
            PathEntry<D, K, V, C> leftMaxNodeEntry = path.get(path.size() - 1);
            PathEntry<D, K, V, C> leftMaxNodeParentEntry = path.get(path.size() - 1);
            replaceAfterDelete(nextNode, leftMaxNodeEntry.key, leftMaxNodeParentEntry.key, entry, path, leftMaxNodeEntry.value, deletingKeyIndex, leftMaxNodeEntry.key.getKeysSize());

        } else {
            throw new IllegalStateException();
        }
    }

    private void replaceAfterDelete(D consumerNode,
                                    D sourceNode,
                                    D sourceNodeParent,
                                    Entry<K, V> entryToReplace,
                                    LinkedList<PathEntry<D, K, V, C>> path,
                                    int sourcePositionIndex,
                                    int consumerKeyIndex,
                                    int sourceKeyIndex) {

        sourceNode.deleteKeyValue(sourceKeyIndex);
        consumerNode.deleteKeyValue(consumerKeyIndex);
        consumerNode.insertKeyValue(consumerKeyIndex, entryToReplace.key, entryToReplace.value);

        if(sourceNode.getKeysSize() < min) {
            balanceAfterDelete(sourceNode, path, sourcePositionIndex, sourceNodeParent);
        }
    }

    private void balanceAfterDelete(D nextNode, LinkedList<PathEntry<D, K, V, C>> path, int positionIndex, D parentNode) {
        D leftNode = nodeProvider.getChildNode(parentNode, positionIndex - 1);
        D rightNode = nodeProvider.getChildNode(parentNode, positionIndex + 1);

        if(rightNode != null && rightNode.getKeysSize() > min) {
            rotate(nextNode, parentNode, rightNode, nextNode.getKeysSize(), positionIndex, 0, 0, nextNode.getChildrenSize());

        } else if(leftNode != null && leftNode.getKeysSize() > min) {
            rotate(nextNode, parentNode, leftNode, 0, positionIndex - 1, leftNode.getKeysSize() - 1, leftNode.getChildrenSize() - 1, 0);

        } else if(rightNode != null) {
            union(nextNode, parentNode, rightNode, path, positionIndex);

        } else if(leftNode != null) {
            union(leftNode, parentNode, nextNode, path, positionIndex - 1);
        }
    }

    private void rotate(D consumerNode,
                        D parentNode,
                        D sourceNode,
                        int consumerKeyIndex,
                        int parentKeyIndex,
                        int sourceKeyIndex,
                        int sourceChildIndex,
                        int consumerChildInsertIndex) {

        Entry<K, V> parentKeyValue = parentNode.deleteKeyValue(parentKeyIndex);
        consumerNode.insertKeyValue(consumerKeyIndex, parentKeyValue.key, parentKeyValue.value);
        Entry<K, V> sourceKeyValue = sourceNode.deleteKeyValue(sourceKeyIndex);
        parentNode.insertKeyValue(parentKeyIndex, sourceKeyValue.key, sourceKeyValue.value);

        if(sourceNode.getChildrenSize() > 0) {
            C deletedChild = sourceNode.deleteChildNode(sourceChildIndex);
            consumerNode.insertChildNode(consumerChildInsertIndex, deletedChild);
        }
    }

    private void union(D firstNode,
                       D parentNode,
                       D secondNode,
                       LinkedList<PathEntry<D, K, V, C>> path,
                       int parentKeyIndex) {

        Entry<K, V> parentKeyValue = parentNode.getKeyValue(parentKeyIndex);
        firstNode.insertKeyValue(firstNode.getKeysSize(), parentKeyValue.key, parentKeyValue.value);

        firstNode.copy(secondNode, 0, secondNode.getKeysSize());

        parentNode.deleteChildNode(parentKeyIndex + 1);

        path.remove(path.size() - 1);

        parentNode.deleteKeyValue(parentKeyIndex);

        if(parentNode == path.get(0).key) {
            if(parentNode.getChildrenSize() > 0 && parentNode.getKeysSize() == 0) {
                path.remove(0);
                path.add(new PathEntry<>(firstNode, 0));
            }
        } else if(parentNode.getKeysSize() < min) {
            int parentPositionIndex = path.get(path.size() - 1).value;
            balanceAfterDelete(parentNode, path, parentPositionIndex, path.get(path.size() - 2).key);
        }
    }

    private Entry<K, V> getMaxKey(D nextNode,
                                  LinkedList<PathEntry<D, K, V, C>> path,
                                  int positionIndex) {

        path.add(new PathEntry<>(nextNode, positionIndex));

        int maxKeyIndex = nextNode.getKeysSize() - 1;
        if(nextNode.isLeaf()) {
            return nextNode.getKeyValue(maxKeyIndex);
        } else {
            return getMaxKey(nodeProvider.getChildNode(nextNode, maxKeyIndex + 1), path, maxKeyIndex + 1);
        }
    }

    private Entry<K, V> getMinKey(D nextNode,
                                  LinkedList<PathEntry<D, K, V, C>> path,
                                  int positionIndex) {

        path.add(new PathEntry<>(nextNode, positionIndex));

        int minKeyIndex = 0;
        if(nextNode.isLeaf()) {
            return nextNode.getKeyValue(minKeyIndex);
        } else {
            return getMinKey(nodeProvider.getChildNode(nextNode, minKeyIndex), path, minKeyIndex);
        }
    }
}
