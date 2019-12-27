package com.devtritus.edu.database.node;

public class Main {
    private static final String LOCALHOST = "127.0.0.1";
    public static void main(String[] args) throws Exception {
        int port = getRandomPort();
        Handler handler = new Handler();
        new DatabaseServer(handler).start(LOCALHOST, port, () -> successCallback(port));
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
