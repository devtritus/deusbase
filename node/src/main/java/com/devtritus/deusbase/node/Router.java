package com.devtritus.deusbase.node;

import com.devtritus.deusbase.api.*;
import com.devtritus.deusbase.node.server.ShardParams;
import com.devtritus.deusbase.node.env.NodeEnvironment;
import com.devtritus.deusbase.node.server.*;
import com.devtritus.deusbase.node.utils.Utils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.devtritus.deusbase.api.ProgramArgNames.*;
import static com.devtritus.deusbase.node.env.NodeSettings.*;

class Router {
    private final static Logger logger = LoggerFactory.getLogger(Router.class);

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
        if(programArgs.contains(CLUSTER_CONFIG_PATH)) {
            String configPath = programArgs.get(CLUSTER_CONFIG_PATH);
            URL url = Router.class.getClassLoader().getResource(configPath);
            try {
                path = Paths.get(url.toURI());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if(!Files.exists(path)) {
                throw new RuntimeException("Shard config was not found by path " + configPath);
            }
        } else {
            try {
                path = env.getFile("router_config.json");
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Shard config was not found", e);
            }
        }

        List<ShardParams> shardParams;
        try {
            shardParams = new ObjectMapper().readValue(path.toFile(), new TypeReference<List<ShardParams>>(){});
        } catch (IOException e) {
            throw new RuntimeException("Broken config", e);
        }

        RouterRequestHandler routerRequestHandler = new RouterRequestHandler(shardParams);

        NodeServer nodeServer = new NodeServer(host, port, proxyRequestHandler(routerRequestHandler), () -> successCallback(routerRequestHandler));
        nodeServer.start();
    }

    private static void successCallback(RouterRequestHandler routerRequestHandler) {
        Utils.printFromFile("banner.txt");
        logger.info("Mode - router");
        routerRequestHandler.runHealthCheck();
    }

    private RequestHandler proxyRequestHandler(NodeRequestHandler nodeRequestHandler) {
        return (Command command, ReadableByteChannel channel) -> {
            RequestBody requestBody = JsonDataConverter.readNodeRequest(channel, RequestBody.class);
            NodeRequest nodeRequest = new NodeRequest(command, requestBody.getArgs());
            return nodeRequestHandler.handle(nodeRequest);
        };
    }
}
