package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.api.ProgramArgs;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import static com.devtritus.deusbase.api.ProgramArgNames.*;
import static com.devtritus.deusbase.node.env.NodeSettings.*;

class JettyServer {
    private HttpRequestHandler handler;

    JettyServer(HttpRequestHandler handler) {
        this.handler = handler;
    }

    void start(String ip, int port, ProgramArgs programArgs, Runnable successCallback) throws Exception {
        int maxThreads = programArgs.getIntegerOrDefault(JETTY_MAX_THREADS, DEFAULT_JETTY_MAX_THREADS);
        int acceptQueueSize = programArgs.getIntegerOrDefault(JETTY_ACCEPT_QUEUE_SIZE, DEFAULT_JETTY_ACCEPT_QUEUE_SIZE);

        Server server = new Server(new QueuedThreadPool(maxThreads));
        ServerConnector connector = new ServerConnector(server);
        connector.setHost(ip);
        connector.setPort(port);
        connector.setAcceptQueueSize(acceptQueueSize);
        server.addConnector(connector);
        server.setHandler(handler);
        server.start();
        successCallback.run();
    }
}
