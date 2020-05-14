package com.devtritus.deusbase.node.env;

import com.devtritus.deusbase.api.ProgramArgs;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.devtritus.deusbase.node.env.NodeSettings.*;
import static com.devtritus.deusbase.api.ProgramArgNames.*;
import static com.devtritus.deusbase.node.utils.Utils.*;

public class NodeEnvironment {
    private final static ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private Path indexPath;
    private Path storagePath;
    private Path nodePath;
    private Path configPath;
    private ObjectNode configRoot;

    public Path getIndexPath() {
        return indexPath;
    }

    public Path getStoragePath() {
        return storagePath;
    }

    public void setUp(ProgramArgs programArgs) {
        String rootPath = programArgs.getOrDefault(ROOT_PATH, DEFAULT_ROOT_PATH);

        Path nodePath = Paths.get(rootPath).toAbsolutePath();

        if(programArgs.contains(GENERATE_FOLDERS)) {
            nodePath = appendToPath(nodePath, ROOT_DIRECTORY_NAME);
            createDirectoryIfNotExist(nodePath);

            if (programArgs.contains(SHARD)) {
                nodePath = Paths.get(rootPath, programArgs.get(SHARD));
                createDirectoryIfNotExist(nodePath);
            }

            if (programArgs.contains(NODE)) {
                String nodeName = programArgs.get(NODE);
                nodePath = appendToPath(nodePath, nodeName);
                createDirectoryIfNotExist(nodePath);
            }
        }

        String schemeName = programArgs.getOrDefault(SCHEME, DEFAULT_SCHEME_NAME);

        nodePath = appendToPath(nodePath, schemeName);
        createDirectoryIfNotExist(nodePath);

        Path configPath = appendToPath(nodePath, CONFIG_FILE_NAME);
        ObjectNode configRoot;
        if(!Files.exists(configPath)) {
            configRoot = initConfig(configPath);
        } else {
            configRoot = readConfig(configPath);
        }

        Path indexPath = appendToPath(nodePath, INDEX_FILE_NAME);
        Path storagePath = appendToPath(nodePath, STORAGE_FILE_NAME);

        boolean indexFileExists = Files.exists(indexPath);
        boolean storageFileExists = Files.exists(storagePath);

        if((!indexFileExists && storageFileExists) || (indexFileExists && !storageFileExists)) {
            throw new IllegalStateException();
        }

        if(!indexFileExists) {
            createFile(indexPath);
            createFile(storagePath);
        }

        this.nodePath = nodePath;
        this.indexPath = indexPath;
        this.storagePath = storagePath;
        this.configPath = configPath;
        this.configRoot = configRoot;
    }

    public String getPropertyOrThrowException(String key) {
        String property = getProperty(key);
        if(property == null) {
            throw new RuntimeException(String.format("Key %s was not found", key));
        }
        return property;
    }

    public String getProperty(String key) {
        JsonNode node = configRoot.get(key);
        if(node != null) {
            return node.asText();
        }
        return null;
    }

    public void putProperty(String key, String value) {
        configRoot.put(key, value);
        writeValue(configPath, configRoot);
    }

    public <T> T putObject(String key, Class<T> type) {
        JsonNode node = configRoot.get(key);
        if(node == null) {
            return null;
        }

        try {
            return objectMapper.treeToValue(node, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> void putArray(String key, List<T> array) {
        ArrayNode arrayNode = configRoot.putArray(key);

        for(T value : array) {
            JsonNode node = objectMapper.valueToTree(value);
            arrayNode.add(node);
        }

        writeValue(configPath, configRoot);
    }

    public <T> List<T> getArray(String key, Class<T> type) {
        JsonNode node = configRoot.get(key);
        if(node == null) {
            return null;
        }

        List<T> values = new ArrayList<>();
        for(JsonNode element : node) {
            try {
                T value = objectMapper.treeToValue(element, type);
                values.add(value);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return values;
    }

    public Path createOrGetNodeFile(String name) {
        Path path = appendToPath(nodePath, name);

        createFileIfNotExist(path);

        return path;
    }

    public Path getFile(String name) throws FileNotFoundException {
        Path path = appendToPath(nodePath, name);

        if(Files.exists(path)) {
            throw new FileNotFoundException(path.toAbsolutePath().normalize().toString());
        }

        return path;
    }

    private Path appendToPath(Path path, String childPathString) {
        String pathString = path.toAbsolutePath().toString();
        return Paths.get(pathString, childPathString);
    }

    private ObjectNode initConfig(Path configPath) {

        ObjectNode objectNode = objectMapper.createObjectNode();
        writeValue(configPath, objectNode);

        return objectNode;
    }

    private ObjectNode readConfig(Path configPath) {
        try {
            return (ObjectNode)objectMapper.readTree(configPath.toFile());
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    void writeValue(Path configPath, JsonNode jsonNode) {
        try {
            objectMapper.writeValue(configPath.toFile(), jsonNode);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
