package com.devtritus.deusbase.node;

import com.devtritus.deusbase.api.*;
import com.devtritus.deusbase.node.api.ShardParams;
import com.devtritus.deusbase.node.env.NodeEnvironment;
import com.devtritus.deusbase.node.server.*;
import com.devtritus.deusbase.node.utils.Utils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.util.List;

import static com.devtritus.deusbase.api.ProgramArgNames.*;
import static com.devtritus.deusbase.node.env.NodeSettings.*;

class Router {
    private ProgramArgs programArgs;

    Router(ProgramArgs programArgs) {
        this.programArgs = programArgs;
    }

    void start() {
        final String host = programArgs.getOrDefault(HOST, DEFAULT_HOST);
        final int port = programArgs.getIntegerOrDefault(PORT, DEFAULT_PORT);

        final NodeEnvironment env = new NodeEnvironment();
        env.setUp(programArgs);

        Path path;
        try {
            path = env.getFile("local_cluster_router_config.json");
        } catch (Exception e) {
            throw new RuntimeException("Shard config was not found", e);
        }

        List<ShardParams> shardParams;
        try {
            shardParams = new ObjectMapper().readValue(path.toFile(), new TypeReference<List<ShardParams>>(){});
        } catch (IOException e) {
            throw new RuntimeException("Broken config", e);
        }

        RouterRequestHandler routerRequestHandler = new RouterRequestHandler(shardParams);

        NodeServer nodeServer = new NodeServer(host, port, proxyRequestHandler(routerRequestHandler), () -> Utils.printFromFile("banner.txt"));
        nodeServer.start();
    }

    private RequestHandler proxyRequestHandler(NodeRequestHandler nodeRequestHandler) {
        return (Command command, ReadableByteChannel channel) -> {
            RequestBody requestBody = JsonDataConverter.readNodeRequest(channel, RequestBody.class);
            NodeRequest nodeRequest = new NodeRequest(command, requestBody.getArgs());
            return nodeRequestHandler.handle(nodeRequest);
        };
    }
}
