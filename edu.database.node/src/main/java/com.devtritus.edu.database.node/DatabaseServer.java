package com.devtritus.edu.database.node;

import org.eclipse.jetty.server.Server;
import java.net.InetSocketAddress;

class DatabaseServer {
    private Handler handler;

    public DatabaseServer(Handler handler) {
        this.handler = handler;
    }

    void start(String ip, int port, Runnable successCallback) throws Exception {
        Server server = new Server(new InetSocketAddress(ip, port));
        server.setAttribute("add-to-start", "logging-log4j");
        server.setHandler(handler);
        server.start();
        successCallback.run();
    }
}
