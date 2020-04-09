package com.devtritus.deusbase.node.env;

import com.devtritus.deusbase.api.ProgramArgs;
import com.devtritus.deusbase.node.server.NodeApi;
import com.devtritus.deusbase.node.storage.DiskStorage;
import com.devtritus.deusbase.node.tree.BTree;
import com.devtritus.deusbase.node.tree.BTreeInitializer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.devtritus.deusbase.node.env.Settings.*;

public class NodeEnvironmentProvider {

    public NodeEnvironment getEnv(ProgramArgs programArgs, String rootPath) {
        Path nodePath = Paths.get(rootPath).toAbsolutePath();

        nodePath = appendToPath(nodePath, DATA_DIRECTORY_NAME);
        createDirectoryIfNotExist(nodePath);

        if(programArgs.contains("shard")) {
            nodePath = Paths.get(rootPath, programArgs.get("shard"));
            createDirectoryIfNotExist(nodePath);
        }

        if(programArgs.contains("id")) {
            String nodeName = "node_" + programArgs.get("id");
            nodePath = appendToPath(nodePath, nodeName);
            createDirectoryIfNotExist(nodePath);
        }

        String schemeName = programArgs.getOrDefault("scheme", DEFAULT_SCHEME_NAME);

        nodePath = appendToPath(nodePath, schemeName);
        createDirectoryIfNotExist(nodePath);

        Path indexFilePath = appendToPath(nodePath, DEFAULT_INDEX_FILE_NAME);
        Path storageFilePath = appendToPath(nodePath, DEFAULT_STORAGE_FILE_NAME);

        boolean indexFileExists = Files.exists(indexFilePath);
        boolean storageFileExists = Files.exists(storageFilePath);

        if((!indexFileExists && storageFileExists) || (indexFileExists && !storageFileExists)) {
            throw new IllegalStateException();
        }

        if(!indexFileExists) {
            createFile(indexFilePath);
            createFile(storageFilePath);
        }

        int treeM = programArgs.getIntegerOrDefault("m", DEFAULT_TREE_M);
        int treeCacheLimit = programArgs.getIntegerOrDefault("tree_cache_limit", DEFAULT_TREE_CACHE_LIMIT);

        BTree<String, List<Long>> tree = BTreeInitializer.init(indexFilePath, treeM, treeCacheLimit);
        DiskStorage storage = new DiskStorage(storageFilePath);

        NodeEnvironment env = new NodeEnvironment();

        NodeApi nodeApi = new NodeApi(tree, storage);

        env.setNodeApi(nodeApi);

        return env;
    }

    private Path appendToPath(Path path, String childPathString) {
        String pathString = path.toAbsolutePath().toString();
        return Paths.get(pathString, childPathString);
    }

    private Path createFile(Path path) {
        try {
            Files.createFile(path);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        return path;
    }

    private Path createDirectoryIfNotExist(Path path) {
        try {
            if(!Files.exists(path)) {
                Files.createDirectory(path);
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        return path;
    }
}
