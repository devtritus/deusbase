package com.devtritus.deusbase.node;

import com.devtritus.deusbase.api.ProgramArgs;
import com.devtritus.deusbase.api.ProgramArgsParser;
import com.devtritus.deusbase.api.RequestBodyHandler;
import com.devtritus.deusbase.node.client.NodeClient;
import com.devtritus.deusbase.node.env.NodeEnvironment;
import com.devtritus.deusbase.node.env.NodeEnvironmentProvider;
import com.devtritus.deusbase.node.server.*;
import com.devtritus.deusbase.node.utils.ActorsLoader;
import com.devtritus.deusbase.node.utils.NodeMode;

import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.devtritus.deusbase.api.ProgramArgNames.*;
import static com.devtritus.deusbase.node.env.Settings.*;

public class Main {
    private final static ExecutorService executor = Executors.newCachedThreadPool();

    public static void main(String[] args) throws Exception {
        ProgramArgs programArgs = ProgramArgsParser.parse(args);

        NodeEnvironmentProvider envProvider = new NodeEnvironmentProvider();
        NodeEnvironment env = envProvider.getEnv(programArgs);

        NodeMode mode = NodeMode.fromText(programArgs.getOrDefault(MODE, DEFAULT_NODE_MODE));
        if(mode == NodeMode.LOAD_DATA) {

            RequestBodyHandler requestBodyHandler = new CrudRequestHandler(env.getNodeApi());
            ActorsLoader.load(programArgs, requestBodyHandler);
        } else {

            executor.submit(new NodeClient(mode, env, programArgs));

            new NodeServer(mode, env, programArgs, Main::printBanner).run();
        }
    }

    private static void printBanner() {
        try {
            InputStream in = Main.class.getClassLoader().getResourceAsStream("banner.txt");
            Scanner scanner = new Scanner(in);
            while(scanner.hasNextLine()) {
                System.out.println(scanner.nextLine());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //TODO: design an initialization cycle
}
