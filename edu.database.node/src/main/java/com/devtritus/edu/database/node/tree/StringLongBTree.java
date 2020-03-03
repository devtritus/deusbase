package com.devtritus.edu.database.node.tree;

public class StringLongBTree extends AbstractBTree<BTreeNode, String, Long, Integer> {
    public StringLongBTree(int m, BTreeNodeProvider<BTreeNode, String, Long, Integer> nodeProvider) {
        super(m, nodeProvider);
    }

    @Override
    boolean isFetchedKeySatisfy(String key, String fetchKey) {
        return key.toLowerCase().startsWith(fetchKey.toLowerCase());
    }
}
