package com.devtritus.deusbase.node.env;

import com.devtritus.deusbase.node.server.NodeApi;

public class NodeEnvironment {
    private NodeApi nodeApi;

    public NodeApi getNodeApi() {
        return nodeApi;
    }

    void setNodeApi(NodeApi nodeApi) {
        this.nodeApi = nodeApi;
    }
}
