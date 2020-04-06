package com.devtritus.edu.database.node.tree;

import java.io.File;

public class BTreeNodeDiskManager {
    private final static int DEFAULT_M = 100;
    private final static int DEFAULT_CACHE_LIMIT = 100;

    private final File file;

    private BTreeNodeDiskProvider provider;
    private int m;

    public BTreeNodeDiskManager(String fileName) {
        this(fileName, DEFAULT_M);
    }

    public BTreeNodeDiskManager(String fileName, int m) {
        this.file = new File(fileName);
        this.m = m;
    }

    public BTreeNodeDiskProvider getNodeProvider() {
        if(provider == null) {
            throw new RuntimeException("Manager wasn't initialized");
        }

        return provider;
    }

    public int initialize() {
        BTreeIndexLoader loader;
        try {
            if (file.exists() && file.length() != 0) {
                loader = BTreeIndexLoader.readIndex(file);
                m = loader.getM();
            } else {
                file.createNewFile();
                loader = BTreeIndexLoader.initIndex(m, file);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        BTreeNodeCache cache = new BTreeNodeCache(DEFAULT_CACHE_LIMIT);
        provider = new BTreeNodeDiskProvider(loader, cache);

        return m;
    }
}
