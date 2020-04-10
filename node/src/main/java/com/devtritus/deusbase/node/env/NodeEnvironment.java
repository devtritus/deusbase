package com.devtritus.deusbase.node.env;

import com.devtritus.deusbase.api.ProgramArgs;
import com.devtritus.deusbase.node.server.NodeApi;
import java.nio.file.Path;

public class NodeEnvironment {
    private final NodeEnvironmentManager envManager;

    private Path configPath;
    private NodeApi nodeApi;
    private NodeConfig config;

    NodeEnvironment(NodeEnvironmentManager envManager) {
        this.envManager = envManager;
    }

    public NodeApi getNodeApi() {
        return nodeApi;
    }

    void setNodeApi(NodeApi nodeApi) {
        this.nodeApi = nodeApi;
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
