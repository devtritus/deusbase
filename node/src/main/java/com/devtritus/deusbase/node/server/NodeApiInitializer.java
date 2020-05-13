package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.node.storage.ValueStorage;
import com.devtritus.deusbase.node.tree.BTree;
import com.devtritus.deusbase.node.tree.BTreeInitializer;
import java.nio.file.Path;
import java.util.List;

public class NodeApiInitializer {
    private final int treeM;
    private final int treeCacheLimit;
    private final Path indexPath;
    private final Path storagePath;
    private final NodeApi api;

    public NodeApiInitializer(int treeM, int treeCacheLimit, Path indexPath, Path storagePath, NodeApi api) {
        this.treeM = treeM;
        this.treeCacheLimit = treeCacheLimit;
        this.indexPath = indexPath;
        this.storagePath = storagePath;
        this.api = api;
    }

    public NodeApi init() {
        BTree<String, List<Long>> tree = BTreeInitializer.init(indexPath, treeM, treeCacheLimit);
        ValueStorage storage = new ValueStorage(storagePath);
        api.setTree(tree);
        api.setStorage(storage);

        return api;
    }
}
