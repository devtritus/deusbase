package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.api.NodeRequest;
import com.devtritus.deusbase.api.NodeResponse;

public interface NodeRequestHandler {
    NodeResponse handle(NodeRequest nodeRequest) throws Exception;
}

