package com.devtritus.deusbase.node.server;

import org.eclipse.jetty.server.Server;
import java.net.InetSocketAddress;

class JettyServer {
    private HttpRequestHandler handler;

    JettyServer(HttpRequestHandler handler) {
        this.handler = handler;
    }

    void start(String ip, int port, Runnable successCallback) throws Exception {
        Server server = new Server(new InetSocketAddress(ip, port));
        server.setHandler(handler);
        server.start();
        successCallback.run();
    }
}
