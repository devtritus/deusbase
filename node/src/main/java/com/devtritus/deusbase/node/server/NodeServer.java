package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.api.RequestBodyHandler;

public class NodeServer {
    public void start(String host, int port, RequestBodyHandler requestBodyHandler, Runnable successCallback) {
        HttpRequestHandler httpRequestHandler = new HttpRequestHandler(requestBodyHandler);

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
