package com.devtritus.deusbase.node.tree;

import com.devtritus.deusbase.node.index.BTreeIndexLoader;

import java.nio.file.Path;
import java.util.List;

import static com.devtritus.deusbase.node.utils.Utils.isEmptyFile;

public abstract class BTreeInitializer {
    public static BTree<String, List<Long>> init(Path path, int m, int cacheLimit) {
        BTreeIndexLoader loader;
        try {
            if (!isEmptyFile(path)) {
                loader = BTreeIndexLoader.readIndex(path);
                int loaderM = loader.getM();
                if(loaderM != m) {
                    throw new IllegalStateException(String.format("M = %s of index is different then argument M = %s", loader, m));
                }
            } else {
                loader = BTreeIndexLoader.initIndex(m, path);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        BTreeNodeCache cache = new BTreeNodeCache(cacheLimit);

        BTreeNodePersistenceProvider provider = new BTreeNodePersistenceProvider(loader, cache);

        return new BTreeImpl(m, provider);
    }
}
