package com.devtritus.edu.database.node.tree;

import com.devtritus.edu.database.node.index.BTreeIndexLoader;
import java.io.File;
import java.util.List;

public abstract class BTreeInitializer {
    private final static int DEFAULT_M = 100;
    private final static int DEFAULT_CACHE_LIMIT = 5000;

    public static BTree<String, List<Long>> init(String fileName) {
        return init(fileName, DEFAULT_M);
    }

    static BTree<String, List<Long>> init(String fileName, int m) {
        BTreeIndexLoader loader;
        File file = new File(fileName);
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

        BTreeNodePersistenceProvider provider = new BTreeNodePersistenceProvider(loader, cache);

        return new BTreeImpl(m, provider);
    }
}
