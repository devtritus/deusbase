package com.devtritus.deusbase.node.tree;

import java.util.List;

class BTreeImpl extends AbstractBTree<BTreeNode, String, List<Long>, Integer> {
    private BTreeNodeProvider<BTreeNode, String, List<Long>, Integer> nodeProvider;

    BTreeImpl(int m, BTreeNodeProvider<BTreeNode, String, List<Long>, Integer> nodeProvider) {
        super(m, nodeProvider);

        this.nodeProvider = nodeProvider;
    }

    BTreeNodeProvider<BTreeNode, String, List<Long>, Integer> getProvider() {
        return nodeProvider;
    }

    @Override
    boolean isFetchedKeySatisfy(String key, String fetchKey) {
        return key.toLowerCase().startsWith(fetchKey.toLowerCase());
    }
}
