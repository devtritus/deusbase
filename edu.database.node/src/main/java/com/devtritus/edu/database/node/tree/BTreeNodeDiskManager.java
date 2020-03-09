package com.devtritus.edu.database.node.tree;

import java.io.File;

public class BTreeNodeDiskManager {
    private final static int DEFAULT_M = 100;
    private final static int DEFAULT_CACHE_LIMIT = 100_000;

    private final File file;

    private BTreeNodeDiskProvider provider;

    public BTreeNodeDiskManager(String fileName) {
        this.file = new File(fileName);
    }

    public BTreeNodeDiskProvider getNodeProvider() {
        if(provider == null) {
            throw new RuntimeException("Manager wasn't initialized");
        }

        return provider;
    }

    public int initialize() {
        int m;
        BTreeIndexLoader loader;
        try {
            if (file.exists() && file.length() != 0) {
                loader = BTreeIndexLoader.read(file);
                m = loader.getM();
            } else {
                file.createNewFile();
                loader = BTreeIndexLoader.init(DEFAULT_M, file);
                m = DEFAULT_M;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        BTreeNodeCache cache = new BTreeNodeCache(DEFAULT_CACHE_LIMIT);
        provider = BTreeNodeDiskProvider.create(loader, cache);

        return m;
    }
}
