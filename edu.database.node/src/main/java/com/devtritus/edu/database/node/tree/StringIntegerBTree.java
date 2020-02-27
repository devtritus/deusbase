package com.devtritus.edu.database.node.tree;

public class StringIntegerBTree extends AbstractBTree<BTreeNode, String, Integer, Integer> {
    public StringIntegerBTree(int m, BTreeNodeProvider<BTreeNode, String, Integer, Integer> nodeProvider) {
        super(m, nodeProvider);
    }

    @Override
    boolean isFetchedKeySatisfy(String key, String fetchKey) {
        return key.toLowerCase().startsWith(fetchKey.toLowerCase());
    }
}
