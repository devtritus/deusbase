package com.devtritus.deusbase.node;

import com.devtritus.deusbase.api.ProgramArgs;
import com.devtritus.deusbase.api.ProgramArgsParser;
import com.devtritus.deusbase.api.RequestBodyHandler;
import com.devtritus.deusbase.node.server.RequestHandler;
import com.devtritus.deusbase.node.server.JettyServer;
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

        NodeApi api = new NodeApi(schemeName + INDEX_POSTFIX, schemeName + STORAGE_POSTFIX);

        RequestBodyHandler requestBodyHandler = new RequestBodyHandler(api);
        RequestHandler requestHandler = new RequestHandler(requestBodyHandler);

        String textMode = programArgs.getOrDefault("mode", "master");
        NodeMode mode = NodeMode.fromText(textMode);

        switch(mode) {
            case LOAD_DATA:
                ActorsLoader.load(programArgs, requestBodyHandler);
                break;
            case SLAVE:
            case MASTER:
                String host = programArgs.getOrDefault("host", DEFAULT_HOST);

                int port;
                if(programArgs.contains("port")) {
                    port = programArgs.getInteger("port");
                } else {
                    port = DEFAULT_PORT;
                }

                new JettyServer(requestHandler).start(host, port, () -> successCallback(host + ":" + port));
                break;
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
