package com.devtritus.deusbase.node;

import com.devtritus.deusbase.api.Api;
import com.devtritus.deusbase.api.ProgramArgs;
import com.devtritus.deusbase.api.RequestBodyHandler;
import com.devtritus.deusbase.node.env.NodeEnvironment;
import com.devtritus.deusbase.node.role.MasterApiDecorator;
import com.devtritus.deusbase.node.role.MasterNode;
import com.devtritus.deusbase.node.role.SlaveApiDecorator;
import com.devtritus.deusbase.node.role.SlaveNode;
import com.devtritus.deusbase.node.server.*;
import com.devtritus.deusbase.node.utils.NodeMode;
import java.io.InputStream;
import java.util.Scanner;

import static com.devtritus.deusbase.api.ProgramArgNames.*;
import static com.devtritus.deusbase.node.env.NodeSettings.DEFAULT_HOST;
import static com.devtritus.deusbase.node.env.NodeSettings.DEFAULT_PORT;

class Node {
    private final NodeServer nodeServer = new NodeServer();

    private NodeMode mode;
    private NodeEnvironment env;
    private ProgramArgs programArgs;

    Node(NodeMode mode, NodeEnvironment env, ProgramArgs programArgs) {
        this.mode = mode;
        this.env = env;
        this.programArgs = programArgs;
    }

    void start() {
        String host = programArgs.getOrDefault(HOST, DEFAULT_HOST);
        int port = programArgs.getIntegerOrDefault(PORT, DEFAULT_PORT);

        final String nodeAddress = host + ":" + port;

        Api<String, String> api;
        RequestBodyHandler nextHandler;

        if(mode == NodeMode.MASTER) {
            MasterNode masterNode = new MasterNode(env);
            masterNode.init();

            api = new MasterApiDecorator<>(env.getNodeApi(), masterNode);
            nextHandler = new MasterRequestHandler(masterNode);

        } else if(mode == NodeMode.SLAVE) {
            String masterAddress = programArgs.get(MASTER_ADDRESS);
            SlaveNode slaveNode = new SlaveNode(env);
            slaveNode.init(nodeAddress, masterAddress);

            api = new SlaveApiDecorator<>(env.getNodeApi(), slaveNode);
            nextHandler = new SlaveRequestHandler(slaveNode);
        } else {
            throw new IllegalStateException();
        }

        nodeServer.start(host, port, api, nextHandler, Node::printBanner);
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
}
