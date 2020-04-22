package com.devtritus.deusbase.node.env;

import com.devtritus.deusbase.api.ProgramArgs;
import java.nio.file.Path;

public class NodeEnvironment {
    private final NodeEnvironmentManager envManager;

    private Path indexFilePath;
    private Path storageFilePath;
    private Path configPath;
    private NodeConfig config;

    NodeEnvironment(NodeEnvironmentManager envManager) {
        this.envManager = envManager;
    }

    public Path getIndexFilePath() {
        return indexFilePath;
    }

    void setIndexFilePath(Path indexFilePath) {
        this.indexFilePath = indexFilePath;
    }

    public Path getStorageFilePath() {
        return storageFilePath;
    }

    void setStorageFilePath(Path storageFilePath) {
        this.storageFilePath = storageFilePath;
    }

    public String getPropertyOrThrowException(String key) {
        String property = getProperty(key);
        if(property == null) {
            throw new RuntimeException(String.format("Key %s wasn't found", key));
        }
        return property;
    }

    public String getProperty(String key) {
        return config.getProperties().get(key);
    }

    public void setProperty(String key, String value) {
        config.getProperties().put(key, value);
        envManager.writeConfig(configPath, config);
    }

    void setConfig(NodeConfig config) {
        this.config = config;
    }

    void setConfigPath(Path configPath) {
        this.configPath = configPath;
    }

    public static NodeEnvironment getEnv(ProgramArgs programArgs) {
        NodeEnvironmentManager envManager = new NodeEnvironmentManager();
        return envManager.getEnv(programArgs);
    }
}
