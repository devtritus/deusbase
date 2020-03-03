package com.devtritus.edu.database.node;

import org.eclipse.jetty.server.Server;
import java.net.InetSocketAddress;

class DatabaseServer {
    private DatabaseRequestHandler handler;

    public DatabaseServer(DatabaseRequestHandler handler) {
        this.handler = handler;
    }

    void start(String ip, int port, Runnable successCallback) throws Exception {
        Server server = new Server(new InetSocketAddress(ip, port));
        server.setHandler(handler);
        server.start();
        successCallback.run();
    }
}
