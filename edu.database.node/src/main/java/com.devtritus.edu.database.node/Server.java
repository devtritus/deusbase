package com.devtritus.edu.database.node;

public class Server {
    public void start(String ip, int port, Runnable successCallback) {
        successCallback.run();
    }
}
