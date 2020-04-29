package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.api.RequestHandler;

public class NodeServer {
    private final String host;
    private final int port;
    private RequestHandler entryPoint;
    private Runnable successCallback;

    public NodeServer(String host, int port, RequestHandler entryPoint, Runnable successCallback) {
        this.host = host;
        this.port = port;
        this.entryPoint = entryPoint;
        this.successCallback = successCallback;
    }

    public void start() {
        HttpRequestHandler httpRequestHandler = new HttpRequestHandler(entryPoint);

        try {
            new JettyServer(httpRequestHandler).start(host, port, () -> {
                successCallback.run();
                System.out.format("\nNode was started on %s:%s\n", host, port);
            });
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private final static int MIN_PORT = 7000;
    private final static int MAX_PORT = 8000;

    private static int getRandomPort() {
        return (int)(MIN_PORT + (Math.random() * (MAX_PORT - MIN_PORT)));
    }
}
