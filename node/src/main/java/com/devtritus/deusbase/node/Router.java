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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
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

        String configPath = programArgs.getOrDefault(ROUTER_CONFIG_PATH, DEFAULT_ROUTER_CONFIG_PATH);
        Path path = Paths.get(configPath);
        if(!Files.exists(path)) {
            throw new RuntimeException("Shard config was not found by path " + configPath);
        }

        List<ShardParams> shardParams;
        try {
            shardParams = new ObjectMapper().readValue(path.toFile(), new TypeReference<List<ShardParams>>(){});
        } catch (IOException e) {
            throw new RuntimeException("Broken config", e);
        }

        for(int i = 0; i < shardParams.size(); i++) {
            if(shardParams.get(i).master == null) {
                throw new IllegalStateException(String.format("Shard %s must have a master url", i));
            }

            if(shardParams.get(i).slaves == null) {
                shardParams.get(i).slaves = Collections.emptyList();
            }
        }

        RouterRequestHandler requestHandler = new RouterRequestHandler(shardParams);
        HttpRequestHandler httpRequestHandler = new HttpRequestHandler(requestHandler);

        NodeServer nodeServer = new NodeServer(host, port, programArgs, httpRequestHandler, () -> successCallback(requestHandler));
        nodeServer.start();
    }

    private static void successCallback(RouterRequestHandler routerRequestHandler) {
        Utils.printFromFile("banner.txt");
        logger.info("Mode - router");
        routerRequestHandler.runHealthCheck();
    }
}
