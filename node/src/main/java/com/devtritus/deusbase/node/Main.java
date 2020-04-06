package com.devtritus.deusbase.node;

import com.devtritus.deusbase.core.RequestBodyHandler;
import com.devtritus.deusbase.node.server.RequestHandler;
import com.devtritus.deusbase.node.server.JettyServer;

public class Main {
    private final static String LOCALHOST = "127.0.0.1";
    private final static String DEFAULT_KEY_FILE_NAME = "node.index";
    private final static String DEFAULT_VALUE_STORAGE_FILE_NAME = "value.storage";

    public static void main(String[] args) throws Exception {
        String keyFileName = DEFAULT_KEY_FILE_NAME;
        String valueStorageFileName = DEFAULT_VALUE_STORAGE_FILE_NAME;
        int port = 7599;//getRandomPort();

        NodeApi api = new NodeApi(keyFileName, valueStorageFileName);

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
