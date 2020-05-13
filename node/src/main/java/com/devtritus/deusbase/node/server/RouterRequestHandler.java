package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.api.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RouterRequestHandler implements NodeRequestHandler {
    private final static int MAX_CYCLE_SIZE = 1_000_000;
    private final static Random random = new Random();

    private final static Map<String, NodeClient> nodeClients = new HashMap<>();
    private final List<ShardParams> shardParams;
    private final int step;

    public RouterRequestHandler(List<ShardParams> shardParams) {
        this.shardParams = shardParams;
        step = MAX_CYCLE_SIZE / shardParams.size();
    }

    public NodeResponse handle(NodeRequest request) throws IOException, UnhandledCommandException {
        final Command command = request.getCommand();
        final String[] args = request.getArgs();

        if(!Command.externalCommands.contains(command)) {
            throw new UnhandledCommandException(command.toString());
        }

        if(args.length == 0) {
            throw new IllegalArgumentException("Arguments were not found");
        }

        String key = args[0];

        NodeClient nodeClient;
        if(command.getType() == CommandType.WRITE) {
            nodeClient = determineNodeClient(key, true);
        } else if(command.getType() == CommandType.READ) {
            nodeClient = determineNodeClient(key, false);
        } else {
            throw new IllegalStateException(String.format("Unhandled type of command: %s", command.toString()));
        }

        return nodeClient.request(command, args);
    }

    private NodeClient determineNodeClient(String key, boolean isMaster) {
        int code = key.hashCode();

        int remainder = code % MAX_CYCLE_SIZE;

        int shardId = 0;
        for(int i = 0; i < shardParams.size(); i++) {
            int nextStep = i * step;
            if(remainder < nextStep) {
                shardId = nextStep;
            }
        }

        ShardParams shardParam = shardParams.get(shardId);

        String url;
        if(isMaster) {
            url = shardParam.master;
        } else {
            int slaveIndex = random.nextInt(shardParams.size());
            url = shardParam.slaves.get(slaveIndex);
        }

        return getOrCreateNodeClient(url);
    }

    private NodeClient getOrCreateNodeClient(String url) {
        NodeClient nodeClient = nodeClients.get(url);
        if(nodeClient == null) {
            nodeClient = new NodeClient(url);
            nodeClients.put(url, nodeClient);
        }

        return nodeClient;
    }
}
