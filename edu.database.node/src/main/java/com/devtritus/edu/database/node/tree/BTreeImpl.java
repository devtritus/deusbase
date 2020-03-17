package com.devtritus.edu.database.node.tree;

import java.util.List;

public class BTreeImpl extends AbstractBTree<BTreeNode, String, List<Long>, Integer> {
    BTreeNodeProvider<BTreeNode, String, List<Long>, Integer> nodeProvider;

    public BTreeImpl(int m, BTreeNodeProvider<BTreeNode, String, List<Long>, Integer> nodeProvider) {
        super(m, nodeProvider);

        this.nodeProvider = nodeProvider;
    }

    @Override
    boolean isFetchedKeySatisfy(String key, String fetchKey) {
        return key.toLowerCase().startsWith(fetchKey.toLowerCase());
    }
}
