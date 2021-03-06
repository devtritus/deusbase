package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class RouterRequestHandler implements RequestHandler {
    private final static Logger logger = LoggerFactory.getLogger(RouterRequestHandler.class);

    private final static int MAX_CYCLE_SIZE = 10000;
    private final static int MAX_RETRIES_COUNT = 3;
    private final static int HEALTH_CHECK_PERIOD = 10; //s

    private final ConcurrentMap<String, RouteParams> routes = new ConcurrentHashMap<>();
    private final ScheduledExecutorService healthCheckExecutor = Executors.newSingleThreadScheduledExecutor();
    private final List<ShardParams> shardParams;
    private final int step;

    public RouterRequestHandler(List<ShardParams> shardParams) {
        this.shardParams = shardParams;
        step = MAX_CYCLE_SIZE / shardParams.size();
    }

    @Override
    public NodeResponse handle(Command command, ReadableByteChannel channel) {
        if(!Command.externalCommands.contains(command)) {
            throw new UnhandledCommandException(command.toString());
        }

        if(command == Command.EXECUTE_REQUESTS) {
            return requestList(channel);
        } else {
            RequestBody requestBody = JsonDataConverter.readNodeRequest(channel, RequestBody.class);
            NodeRequest request = new NodeRequest(command, requestBody.getArgs());
            if(command == Command.SEARCH) {
                return requestToAll(request);
            } else {
                return requestSingle(request);
            }
        }
    }

    private NodeResponse requestToAll(final NodeRequest request) {
        List<CompletableFuture<NodeResponse>> futures = new ArrayList<>();
        for(int i = 0; i < shardParams.size(); i++) {
            final int shardId = i;
            CompletableFuture<NodeResponse> future =
                    CompletableFuture.supplyAsync(() -> requestSingle(shardId, request, shardParams.get(shardId)));
            futures.add(future);
        }

        CompletableFuture<Void> compositeFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        compositeFuture.join();
        NodeResponse aggregatedResponse = NodeResponse.ok();
        aggregatedResponse.setData(new HashMap<>());
        for(CompletableFuture<NodeResponse> future : futures) {
            try {
                NodeResponse nodeResponse = future.get();
                if(nodeResponse.getCode() == ResponseStatus.OK.getCode()) {
                    aggregatedResponse.getData().putAll(nodeResponse.getData());
                } else {
                    logger.error("Unexpected code: {}, data: {}", nodeResponse.getCode(), nodeResponse.getData());
                }
            } catch(Exception e) {
                logger.error("Error occurred", e);
            }
        }

        return aggregatedResponse;
    }

    private NodeResponse requestList(ReadableByteChannel channel) {
        List<NodeRequest> requests = JsonDataConverter.readList(channel, NodeRequest.class);
        Map<String, List<NodeRequest>> masterUrlToRequestsMap = new HashMap<>();
        for(ShardParams shardParams : shardParams) {
            masterUrlToRequestsMap.put(shardParams.master.getHttpUrl(), new ArrayList<>());
        }

        for(NodeRequest request : requests) {
            String key = request.getArgs()[0];
            int shardId = determineShardId(key);
            ShardParams shardParam = shardParams.get(shardId);
            masterUrlToRequestsMap.get(shardParam.master.getHttpUrl()).add(request);
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        masterUrlToRequestsMap.forEach((masterUrl, request) -> {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                NodeClient client = createOrGetRouter(masterUrl).getClient();
                try {
                    client.executeRequests(request);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            futures.add(future);
        });

        CompletableFuture<Void> compositeFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        compositeFuture.join();

        return NodeResponse.ok();
    }

    private NodeResponse requestSingle(NodeRequest request) {
        final Command command = request.getCommand();
        final String[] args = request.getArgs();

        if(!Command.externalCommands.contains(command)) {
            throw new UnhandledCommandException(command.toString());
        }

        if(args.length == 0) {
            throw new IllegalArgumentException("Arguments were not found");
        }

        String key = args[0];

        int shardId = determineShardId(key);
        ShardParams shardParam = shardParams.get(shardId);

        return requestSingle(shardId, request, shardParam);
    }

    private NodeResponse requestSingle(int shardId, NodeRequest request, ShardParams shardParam) {
        final Command command = request.getCommand();
        final String[] args = request.getArgs();
        logger.debug("Select shard {}", shardId);

        if(command.getType() == CommandType.WRITE) {
            String masterUrl = shardParam.master.getHttpUrl();
            RouteParams masterRoute = createOrGetRouter(masterUrl);
            try {
                logger.debug("Send WRITE command '{} {}' by route {}", command, Arrays.toString(args), masterRoute);
                NodeResponse response = sendRequest(masterRoute, command, args, masterUrl);
                masterRoute.incrementRequestsCount();
                return response;
            } catch (ServiceUnavailableException e) {
                logger.error("Node at url {} is unavailable", masterUrl, e);
                throw e;
            } catch (IOException e) {
                throw new ServiceUnavailableException(String.format("Cannot handle command '%s %s', master %s is offline",
                        command, Arrays.toString(args), masterUrl));
            }
        } else if(command.getType() == CommandType.READ) {
            List<String> slaveUrls = shardParam.slaves.stream()
                    .map(HostPort::getHttpUrl)
                    .collect(Collectors.toList());

            Collections.shuffle(slaveUrls);

            List<String> urls = new ArrayList<>(slaveUrls);
            urls.add(shardParam.master.getHttpUrl()); //if all slaves unavailable then read from master
            for(String url : urls) {
                RouteParams route = createOrGetRouter(url);
                if(route.isOnline()) {
                    try {
                        logger.debug("Send READ command '{} {}' by route {}", command, Arrays.toString(args), route);
                        NodeResponse response = sendRequest(route, command, args, url);
                        route.incrementRequestsCount();
                        return response;
                    } catch (IOException e) {
                        if(route.isOnline()) {
                            route.setOnline(false);
                            logger.warn("Node at url {} is offline", url);
                        }
                    }
                }
            }

            throw new ServiceUnavailableException(String.format("Cannot handle command '%s %s', nodes %s are offline",
                    command, Arrays.toString(args), urls));

        } else {
            throw new IllegalStateException(String.format("Unhandled type of command: %s", command.toString()));
        }
    }

    public void runHealthCheck() {
        healthCheckExecutor.scheduleAtFixedRate(this::healthCheck, 0, HEALTH_CHECK_PERIOD, TimeUnit.SECONDS);
    }

    private NodeResponse sendRequest(RouteParams route, Command command, String[] args, String url) throws IOException {
        int retriesCount = 0;
        while(true) {
            try {
                NodeResponse response = route.getClient().request(command, args);

                //wrap server error
                if(response.getCode() == ResponseStatus.SERVER_ERROR.getCode()) {
                    response.setData("error", "Error message from " + url + ": " + response.getData().get("error").get(0));
                }

                return response;
            } catch (IOException e) {
                retriesCount++;
                logger.error("Cannot send request by client {}", route, e);
                if(retriesCount > MAX_RETRIES_COUNT) {
                    throw e;
                }
            }
        }
    }

    private RouteParams createOrGetRouter(String url) {
        RouteParams route = routes.get(url);
        if(route == null) {
            route = new RouteParams();
            route.setOnline(true); //route is online by default
            route.setClient(new NodeClient(url));
            routes.put(url, route);
        }

        return route;
    }

    private int determineShardId(String key) {
        int code = key.hashCode() & 0x7fffffff;

        int remainder = code % MAX_CYCLE_SIZE;

        int shardId = 0;
        for(int i = 0; i < shardParams.size(); i++) {
            int nextStep = i * step;
            if(remainder < nextStep) {
                shardId = i;
            }
        }

        return shardId;
    }

    private void healthCheck() {
        List<Map.Entry<String, RouteParams>> offlineRoutes = routes.entrySet().stream()
                .filter(entry -> !entry.getValue().isOnline())
                .collect(Collectors.toList());

        if(offlineRoutes.isEmpty()) {
            logger.debug("List of offline routes is empty");
            return;
        }

        for(Map.Entry<String, RouteParams> entry : offlineRoutes) {
            NodeClient client = null;
            try {
                final String url = entry.getKey();
                final RouteParams route = entry.getValue();

                client = route.getClient();
                client.request(Command.HEARTBEAT);

                route.setOnline(true);
                logger.info("Node at url {} is online", url);
            } catch(Exception e) {
                logger.debug("Heartbeat failed. Client - {}", client);
            }
        }
    }
}
