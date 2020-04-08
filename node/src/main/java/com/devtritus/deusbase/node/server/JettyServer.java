package com.devtritus.deusbase.node.server;

import org.eclipse.jetty.server.Server;
import java.net.InetSocketAddress;

public class JettyServer {
    private HttpRequestHandler handler;

    public JettyServer(HttpRequestHandler handler) {
        this.handler = handler;
    }

    public void start(String ip, int port, Runnable successCallback) throws Exception {
        Server server = new Server(new InetSocketAddress(ip, port));
        server.setHandler(handler);
        server.start();
        successCallback.run();
    }
}
