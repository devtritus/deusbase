package com.devtritus.edu.database.node.tree;

import java.util.*;

abstract class AbstractBTree<D extends AbstractBTreeNode<K, V, C>, K extends Comparable<K>, V, C> implements BTree<K, V> {
    private final BTreeNodeProvider<D, K, V, C> nodeProvider;
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
        D root = nodeProvider.getRootNode();
        fetch(key, root, result);
        return result;
    }

    @Override
    public V searchByKey(K key) {
        D root = nodeProvider.getRootNode();
        return searchByKey(key, root);
    }

    @Override
    public K add(K key, V value) {
        LinkedList<PathEntry<D, K, V, C>> path = new LinkedList<>();
        D root = nodeProvider.getRootNode();
        add(key, value, root, path, null);

        nodeProvider.flush();

        return key;
    }

    @Override
    public boolean deleteKey(K key) {
        LinkedList<PathEntry<D, K, V, C>> path = new LinkedList<>();
        D root = nodeProvider.getRootNode();
        boolean result = delete(key, root, path, null, null);

        nodeProvider.flush();

        return result;
    }

    @Override
    public boolean isEmpty() {
        D root = nodeProvider.getRootNode();
        if(root.getKeysSize() == 0 && root.getChildrenSize() != 0)  {
            throw new IllegalStateException();
        }
        return root.getKeysSize() == 0;
    }

    private void fetch(K key, D nextNode, Map<K, V> result) {

        int lastChildIndex = 0;
        boolean found = false;
        List<K> keys = nextNode.getKeys();
        for (int i = 0; i < keys.size(); i++) {
            Pair<K, V> entry = nextNode.getKeyValue(i);
            if (isFetchedKeySatisfy(entry.first, key)) {
                found = true;
                result.put(entry.first, entry.second);

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
            return nextNode.getKeyValue(index).second;
        } else {
            if(nextNode.isLeaf()) {
                return null;
            }

            D child = nodeProvider.getChildNode(nextNode, -index - 1);
            return searchByKey(key, child);
        }
    }

    private void add(K key, V value, D nextNode, LinkedList<PathEntry<D, K, V, C>> path, Integer position) {
        PathEntry<D, K, V, C> entry = new PathEntry<>(nextNode, position);
        path.add(entry);

        if(nextNode.isLeaf()) {
            doAdd(key, value, nextNode, path);
        } else {
            int index = nextNode.searchKey(key);
            if(index > -1) {
                nodeProvider.putKeyValueToNode(entry.first, index, key, value);
                return;
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
        PathEntry<D, K, V, C> nextParentEntry;
        int insertionIndex;

        List<PathEntry<D, K, V, C>> parents = new ArrayList<>(path.subList(0, path.size() - 1));

        if(parents.isEmpty()) {
            D nextParent = nodeProvider.createNode(nextNode.getLevel() + 1);
            nextParent.setRoot(true);
            nextNode.setRoot(false);
            nextParentEntry = new PathEntry<>(nextParent, null);
            path.addFirst(nextParentEntry);
            nodeProvider.insertChildNode(nextParent, nextNode, 0);
            insertionIndex = 0;
        } else {
            nextParentEntry = parents.get(parents.size() - 1);
            insertionIndex = path.get(path.size() - 1).second;
        }

        D rightNode = nodeProvider.createNode(nextNode.getLevel());
        rightNode.copy(nextNode, min + 1, nextNode.getKeysSize());
        nextNode.delete(min + 1, nextNode.getKeysSize());

        nodeProvider.insertChildNode(nextParentEntry.first, rightNode, insertionIndex + 1);

        Pair<K, V> middleKeyValue = nextNode.deleteKeyValue(min);

        path.remove(path.size() - 1);

        doAdd(middleKeyValue.first, middleKeyValue.second, nextParentEntry.first, path);
    }

    private boolean delete(K key, D nextNode, LinkedList<PathEntry<D, K, V, C>> path, Integer position, D parent) {
        PathEntry<D, K, V, C> entry = new PathEntry<>(nextNode, position);
        path.add(entry);

        int index = nextNode.searchKey(key);

        if(index > -1) {
            if(nextNode.isLeaf()) {
                nextNode.deleteKeyValue(index);

                if(nextNode.getKeysSize() < min && parent != null) {
                    balanceAfterDelete(nextNode, path, position, parent);
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
            Pair<K, V> entry = getMinKey(rightChildNode, path, deletingKeyIndex + 1);
            PathEntry<D, K, V, C> rightMinNodeEntry = path.get(path.size() - 1);
            PathEntry<D, K, V, C> rightMinNodeParentEntry = path.get(path.size() - 2);
            replaceAfterDelete(nextNode, rightMinNodeEntry.first, rightMinNodeParentEntry.first, entry, path, rightMinNodeEntry.second, deletingKeyIndex, 0);

        } else if(leftChildNode != null) {
            Pair<K, V> entry = getMaxKey(leftChildNode, path, deletingKeyIndex);
            PathEntry<D, K, V, C> leftMaxNodeEntry = path.get(path.size() - 1);
            PathEntry<D, K, V, C> leftMaxNodeParentEntry = path.get(path.size() - 1);
            replaceAfterDelete(nextNode, leftMaxNodeEntry.first, leftMaxNodeParentEntry.first, entry, path, leftMaxNodeEntry.second, deletingKeyIndex, leftMaxNodeEntry.first.getKeysSize());

        } else {
            throw new IllegalStateException();
        }
    }

    private void replaceAfterDelete(D consumerNode,
                                    D sourceNode,
                                    D sourceNodeParent,
                                    Pair<K, V> entryToReplace,
                                    LinkedList<PathEntry<D, K, V, C>> path,
                                    int sourcePositionIndex,
                                    int consumerKeyIndex,
                                    int sourceKeyIndex) {

        sourceNode.deleteKeyValue(sourceKeyIndex);
        consumerNode.deleteKeyValue(consumerKeyIndex);
        consumerNode.insertKeyValue(consumerKeyIndex, entryToReplace.first, entryToReplace.second);

        if(sourceNode.getKeysSize() < min) {
            balanceAfterDelete(sourceNode, path, sourcePositionIndex, sourceNodeParent);
        }
    }

    private void balanceAfterDelete(D nextNode, LinkedList<PathEntry<D, K, V, C>> path, int position, D parentNode) {
        D leftNode = nodeProvider.getChildNode(parentNode, position - 1);
        D rightNode = nodeProvider.getChildNode(parentNode, position + 1);

        if(rightNode != null && rightNode.getKeysSize() > min) {
            rotate(nextNode, parentNode, rightNode, nextNode.getKeysSize(), position, 0, 0, nextNode.getChildrenSize());

        } else if(leftNode != null && leftNode.getKeysSize() > min) {
            rotate(nextNode, parentNode, leftNode, 0, position - 1, leftNode.getKeysSize() - 1, leftNode.getChildrenSize() - 1, 0);

        } else if(rightNode != null) {
            union(nextNode, parentNode, rightNode, path, position);

        } else if(leftNode != null) {
            union(leftNode, parentNode, nextNode, path, position - 1);
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

        Pair<K, V> parentKeyValue = parentNode.deleteKeyValue(parentKeyIndex);
        consumerNode.insertKeyValue(consumerKeyIndex, parentKeyValue.first, parentKeyValue.second);
        Pair<K, V> sourceKeyValue = sourceNode.deleteKeyValue(sourceKeyIndex);
        parentNode.insertKeyValue(parentKeyIndex, sourceKeyValue.first, sourceKeyValue.second);

        if(sourceNode.getChildrenSize() > 0) {
            C deletedChild = sourceNode.deleteChild(sourceChildIndex);
            consumerNode.insertChild(consumerChildInsertIndex, deletedChild);
        }
    }

    private void union(D firstNode,
                       D parentNode,
                       D secondNode,
                       LinkedList<PathEntry<D, K, V, C>> path,
                       int parentKeyIndex) {

        Pair<K, V> parentKeyValue = parentNode.getKeyValue(parentKeyIndex);
        firstNode.insertKeyValue(firstNode.getKeysSize(), parentKeyValue.first, parentKeyValue.second);

        firstNode.copy(secondNode, 0, secondNode.getKeysSize());

        parentNode.deleteChild(parentKeyIndex + 1);

        path.remove(path.size() - 1);

        parentNode.deleteKeyValue(parentKeyIndex);

        if(parentNode == path.get(0).first) {
            if(parentNode.getChildrenSize() > 0 && parentNode.getKeysSize() == 0) {
                path.remove(0);
                path.add(new PathEntry<>(firstNode, 0));

                parentNode.setRoot(false);
                firstNode.setRoot(true);
            }
        } else if(parentNode.getKeysSize() < min) {
            int parentPositionIndex = path.get(path.size() - 1).second;
            balanceAfterDelete(parentNode, path, parentPositionIndex, path.get(path.size() - 2).first);
        }
    }

    private Pair<K, V> getMaxKey(D nextNode,
                                 LinkedList<PathEntry<D, K, V, C>> path,
                                 int position) {

        path.add(new PathEntry<>(nextNode, position));

        int maxKeyIndex = nextNode.getKeysSize() - 1;
        if(nextNode.isLeaf()) {
            return nextNode.getKeyValue(maxKeyIndex);
        } else {
            return getMaxKey(nodeProvider.getChildNode(nextNode, maxKeyIndex + 1), path, maxKeyIndex + 1);
        }
    }

    private Pair<K, V> getMinKey(D nextNode,
                                 LinkedList<PathEntry<D, K, V, C>> path,
                                 int position) {

        path.add(new PathEntry<>(nextNode, position));

        int minKeyIndex = 0;
        if(nextNode.isLeaf()) {
            return nextNode.getKeyValue(minKeyIndex);
        } else {
            return getMinKey(nodeProvider.getChildNode(nextNode, minKeyIndex), path, minKeyIndex);
        }
    }
}
