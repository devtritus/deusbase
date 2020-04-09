package com.devtritus.deusbase.node.env;

import com.devtritus.deusbase.node.server.NodeApi;

public class NodeEnvironment {
    private NodeApi nodeApi;

    private NodeConfig config;

    public NodeApi getNodeApi() {
        return nodeApi;
    }

    void setNodeApi(NodeApi nodeApi) {
        this.nodeApi = nodeApi;
    }

    public NodeConfig getConfig() {
        return config;
    }

    void setConfig(NodeConfig config) {
        this.config = config;
    }
}
