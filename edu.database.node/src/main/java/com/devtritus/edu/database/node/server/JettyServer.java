package com.devtritus.edu.database.node.server;

import org.eclipse.jetty.server.Server;
import java.net.InetSocketAddress;

public class JettyServer {
    private RequestHandler handler;

    public JettyServer(RequestHandler handler) {
        this.handler = handler;
    }

    public void start(String ip, int port, Runnable successCallback) throws Exception {
        Server server = new Server(new InetSocketAddress(ip, port));
        server.setHandler(handler);
        server.start();
        successCallback.run();
    }
}
