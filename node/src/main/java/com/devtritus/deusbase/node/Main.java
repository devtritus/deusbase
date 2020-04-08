package com.devtritus.deusbase.node;

import com.devtritus.deusbase.api.Api;
import com.devtritus.deusbase.api.ProgramArgs;
import com.devtritus.deusbase.api.ProgramArgsParser;
import com.devtritus.deusbase.api.RequestBodyHandler;
import com.devtritus.deusbase.node.server.*;
import com.devtritus.deusbase.node.utils.ActorsLoader;
import com.devtritus.deusbase.node.utils.NodeMode;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    private final static String DEFAULT_HOST = "127.0.0.1";
    private final static String DEFAULT_SCHEME_NAME = "default";
    private final static int DEFAULT_PORT = 7599;

    private final static String INDEX_POSTFIX = ".index";
    private final static String STORAGE_POSTFIX = ".storage";

    public static void main(String[] args) throws Exception {
        ProgramArgs programArgs = ProgramArgsParser.parse(args);
        String schemeName = programArgs.getOrDefault("scheme", DEFAULT_SCHEME_NAME);

        NodeApi nodeApi = new NodeApi(schemeName + INDEX_POSTFIX, schemeName + STORAGE_POSTFIX);

        String textMode = programArgs.getOrDefault("mode", "master");
        NodeMode mode = NodeMode.fromText(textMode);

        if(mode == NodeMode.LOAD_DATA) {
            RequestBodyHandler requestBodyHandler = new CrudRequestHandler(nodeApi);
            ActorsLoader.load(programArgs, requestBodyHandler);
        } else {
            String host = programArgs.getOrDefault("host", DEFAULT_HOST);

            int port;
            if(programArgs.contains("port")) {
                port = programArgs.getInteger("port");
            } else {
                port = DEFAULT_PORT;
            }

            ServiceApi serviceApi = new ServiceApi();

            Api<String, String> api;
            RequestBodyHandler nextHandler;
            if(mode == NodeMode.MASTER) {
                api = new MasterApiDecorator<>(nodeApi);
                nextHandler = new MasterRequestHandler(serviceApi);
            } else if(mode == NodeMode.SLAVE) {
                api = new SlaveApiDecorator<>(nodeApi);
                nextHandler = new SlaveRequestHandler(serviceApi);
            } else {
                throw new IllegalArgumentException(String.format("Unhandled server mode %s", mode));
            }

            CrudRequestHandler requestBodyHandler = new CrudRequestHandler(api);

            requestBodyHandler.setNextHandler(nextHandler);

            HttpRequestHandler httpRequestHandler = new HttpRequestHandler(requestBodyHandler);

            new JettyServer(httpRequestHandler).start(host, port, () -> successCallback(host + ":" + port));
        }
    }

    private final static int MIN_PORT = 7000;
    private final static int MAX_PORT = 8000;

    private static int getRandomPort() {
        return (int)(MIN_PORT + (Math.random() * (MAX_PORT - MIN_PORT)));
    }

    private static void successCallback(String address) {
        try {
            URI uri = Main.class.getClassLoader().getResource("banner.txt").toURI();
            List<String> bannerLines = Files.readAllLines(Paths.get(uri));
            for (String line : bannerLines) {
                System.out.println(line);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.out.println("\nNode was started on " + address);
    }
}
