package com.devtritus.edu.database.node;

import com.devtritus.edu.database.core.RequestBodyHandler;
import com.devtritus.edu.database.node.server.RequestHandler;
import com.devtritus.edu.database.node.server.JettyServer;

public class Main {
    private static final String LOCALHOST = "127.0.0.1";
    public static void main(String[] args) throws Exception {
        int port = 7599;//getRandomPort();

        NodeApi api = new NodeApi();

        RequestBodyHandler requestBodyHandler = new RequestBodyHandler(api);
        RequestHandler requestHandler = new RequestHandler(requestBodyHandler);

        new JettyServer(requestHandler).start(LOCALHOST, port, () -> successCallback(port));
    }

    private final static int MIN_PORT = 7000;
    private final static int MAX_PORT = 8000;

    private static int getRandomPort() {
        return (int)(MIN_PORT + (Math.random() * (MAX_PORT - MIN_PORT)));
    }

    private static void successCallback(int port) {
        System.out.println("Node was started on port " + port);
    }
}
