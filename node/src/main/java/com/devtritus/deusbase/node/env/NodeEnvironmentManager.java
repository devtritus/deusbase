package com.devtritus.deusbase.node.env;

import com.devtritus.deusbase.api.ProgramArgs;
import com.devtritus.deusbase.node.server.NodeApi;
import com.devtritus.deusbase.node.storage.StringStorage;
import com.devtritus.deusbase.node.tree.BTree;
import com.devtritus.deusbase.node.tree.BTreeInitializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.devtritus.deusbase.node.env.NodeSettings.*;
import static com.devtritus.deusbase.api.ProgramArgNames.*;

class NodeEnvironmentManager {
    private final static ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    NodeEnvironment getEnv(ProgramArgs programArgs) {
        String rootPath = programArgs.getOrDefault(ROOT_PATH, DEFAULT_ROOT_PATH);

        Path nodePath = Paths.get(rootPath).toAbsolutePath();
        nodePath = appendToPath(nodePath, DATA_DIRECTORY_NAME);
        createDirectoryIfNotExist(nodePath);

        if(programArgs.contains(SHARD)) {
            nodePath = Paths.get(rootPath, programArgs.get(SHARD));
            createDirectoryIfNotExist(nodePath);
        }

        if(programArgs.contains(ID)) {
            String nodeName = "node_" + programArgs.get(ID);
            nodePath = appendToPath(nodePath, nodeName);
            createDirectoryIfNotExist(nodePath);
        }

        String schemeName = programArgs.getOrDefault(SCHEME, DEFAULT_SCHEME_NAME);

        nodePath = appendToPath(nodePath, schemeName);
        createDirectoryIfNotExist(nodePath);

        NodeEnvironment env = new NodeEnvironment(this);

        Path configPath = appendToPath(nodePath, CONFIG_FILE_NAME);
        NodeConfig config;
        if(Files.exists(configPath)) {
            config = readConfig(configPath);
        } else {
            config = initConfig(configPath);
        }

        env.setConfig(config);
        env.setConfigPath(configPath);

        Path indexFilePath = appendToPath(nodePath, INDEX_FILE_NAME);
        Path storageFilePath = appendToPath(nodePath, STORAGE_FILE_NAME);

        boolean indexFileExists = Files.exists(indexFilePath);
        boolean storageFileExists = Files.exists(storageFilePath);

        if((!indexFileExists && storageFileExists) || (indexFileExists && !storageFileExists)) {
            throw new IllegalStateException();
        }

        if(!indexFileExists) {
            createFile(indexFilePath);
            createFile(storageFilePath);
        }

        int treeM = programArgs.getIntegerOrDefault(TREE_M, DEFAULT_TREE_M);
        int treeCacheLimit = programArgs.getIntegerOrDefault(TREE_CACHE_LIMIIT, DEFAULT_TREE_CACHE_LIMIT);

        BTree<String, List<Long>> tree = BTreeInitializer.init(indexFilePath, treeM, treeCacheLimit);
        StringStorage storage = new StringStorage(storageFilePath);

        NodeApi nodeApi = new NodeApi(tree, storage);

        env.setNodeApi(nodeApi);

        return env;
    }

    private Path appendToPath(Path path, String childPathString) {
        String pathString = path.toAbsolutePath().toString();
        return Paths.get(pathString, childPathString);
    }

    private NodeConfig initConfig(Path configPath) {
        final NodeConfig config = new NodeConfig();

        Map<String, String> properties = new HashMap<>();
        properties.put("uuid", UUID.randomUUID().toString());
        config.setProperties(properties);

        writeConfig(configPath, config);

        return config;
    }

    void writeConfig(Path configPath, NodeConfig config) {
        try {
            objectMapper.writeValue(configPath.toFile(), config);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private NodeConfig readConfig(Path configPath) {
        try {
            return objectMapper.readValue(configPath.toFile(), NodeConfig.class);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
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
