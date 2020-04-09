package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.api.Api;
import com.devtritus.deusbase.api.ProgramArgs;
import com.devtritus.deusbase.api.RequestBodyHandler;
import com.devtritus.deusbase.node.env.NodeEnvironment;
import com.devtritus.deusbase.node.utils.NodeMode;

import static com.devtritus.deusbase.api.ProgramArgNames.HOST;
import static com.devtritus.deusbase.api.ProgramArgNames.PORT;
import static com.devtritus.deusbase.node.env.Settings.*;

public class NodeServer implements Runnable {
    private final NodeMode mode;
    private final NodeEnvironment env;
    private final ProgramArgs programArgs;
    private final Runnable successCallback;

    public NodeServer(NodeMode mode, NodeEnvironment env, ProgramArgs programArgs, Runnable successCallback) {
        this.mode = mode;
        this.env = env;
        this.programArgs = programArgs;
        this.successCallback = successCallback;
    }

    public void run() {
        String host = programArgs.getOrDefault(HOST, DEFAULT_HOST);
        int port = programArgs.getIntegerOrDefault(PORT, DEFAULT_PORT);

        ServiceApi serviceApi = new ServiceApi();

        Api<String, String> api;
        RequestBodyHandler nextHandler;
        if(mode == NodeMode.MASTER) {
            api = new MasterApiDecorator<>(env.getNodeApi());
            nextHandler = new MasterRequestHandler(serviceApi);
        } else if(mode == NodeMode.SLAVE) {
            api = new SlaveApiDecorator<>(env.getNodeApi());
            nextHandler = new SlaveRequestHandler(serviceApi);
        } else {
            throw new IllegalArgumentException(String.format("Unhandled server mode %s", mode));
        }

        CrudRequestHandler requestBodyHandler = new CrudRequestHandler(api);

        requestBodyHandler.setNextHandler(nextHandler);

        HttpRequestHandler httpRequestHandler = new HttpRequestHandler(requestBodyHandler);

        try {
            new JettyServer(httpRequestHandler).start(host, port, () -> {
                successCallback.run();
                System.out.format("\nNode was started on %s:%s", host, port);
            });
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private final static int MIN_PORT = 7000;
    private final static int MAX_PORT = 8000;

    private static int getRandomPort() {
        return (int)(MIN_PORT + (Math.random() * (MAX_PORT - MIN_PORT)));
    }
}
