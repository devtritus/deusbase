package com.devtritus.edu.database.node.tree;

import com.devtritus.edu.database.node.utils.Pair;
import com.devtritus.edu.database.node.utils.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class AbstractBTreeNode<K extends Comparable<K>, V, C> {

    private final C nodeId;
    private final int level;

    private boolean root;
    private boolean modified;
    private List<K> keys = new ArrayList<>();
    private List<V> values = new ArrayList<>();
    private List<C> children = new ArrayList<>();

    AbstractBTreeNode(C nodeId, int level, boolean modified) {
        this.nodeId = nodeId;
        this.level = level;
        this.modified = modified;
    }

    C getNodeId() {
        return nodeId;
    }

    boolean isLeaf() {
        return getLevel() == 0;
    }

    boolean isRoot() {
        return root;
    }

    int getLevel() {
        return level;
    }

    boolean isModified() {
        return modified;
    }

    List<K> getKeys() {
        return new ArrayList<>(keys);
    }

    List<V> getValues() {
        return new ArrayList<>(values);
    }

    List<C> getChildren() {
        return new ArrayList<>(children);
    }

    int getChildrenSize() {
        return children.size();
    }

    int getKeysSize() {
        return getKeys().size();
    }

    void setRoot(boolean root) {
        markAsModified();
        this.root = root;
    }

    void markAsNotModified() {
        modified = false;
    }

    void markAsModified() {
        modified = true;
    }

    int searchKey(K key) {
        return Collections.binarySearch(keys, key);
    }

    Pair<K, V> getKeyValue(int index) {
        K key = keys.get(index);
        V value = values.get(index);
        return new Pair<>(key, value);
    }

    V getValue(K key) {
        int index = searchKey(key);
        if(index < 0) {
            return null;
        } else {
            return values.get(index);
        }
    }

    void putKeyValue(int index, K key, V value) {
        if(index > -1) {
            values.set(index, value);
        } else {
            int insertionIndex = -index - 1;
            insertKeyValue(insertionIndex, key, value);
        }

        markAsModified();
    }

    void insertKeyValue(int index, K key, V value) {
        Utils.insertToList(keys, key, index);
        Utils.insertToList(values, value, index);

        markAsModified();
    }

    Pair<K, V> deleteKeyValue(int index) {
        if(index < 0) {
            throw new IllegalStateException(String.format("Key by index %s not found", index));
        }

        Pair<K, V> keyValue = getKeyValue(index);

        keys.remove(index);
        values.remove(index);

        markAsModified();

        return keyValue;
    }

    void insertChild(int index, C child) {
        Utils.insertToList(children, child, index);

        markAsModified();
    }

    C deleteChild(int index) {
        C child = children.remove(index);

        markAsModified();

        return child;
    }

    void putKeyValue(K key, V value) {
        int index = searchKey(key);
        putKeyValue(index, key, value);

        markAsModified();
    }

    void delete(int start, int end) {
        keys.subList(start, end).clear();
        values.subList(start, end).clear();
        if(!children.isEmpty()) {
            children.subList(start, end + 1).clear();
        }

        markAsModified();
    }

    void copy(AbstractBTreeNode<K, V, C> fromNode, int start, int end) {
        keys.addAll(fromNode.getKeys().subList(start, end));
        values.addAll(fromNode.getValues().subList(start, end));
        if(!fromNode.getChildren().isEmpty()) {
            children.addAll(fromNode.getChildren().subList(start, end + 1));
        }

        markAsModified();
    }

    void setKeys(List<K> keys) {
        this.keys = keys;
    }

    void setValues(List<V> values) {
        this.values = values;
    }

    void setChildren(List<C> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return String.format("node_%s(level=%s) keys=%s", nodeId, level, keys);
    }
}
