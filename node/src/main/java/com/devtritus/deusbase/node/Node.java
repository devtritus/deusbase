package com.devtritus.deusbase.node;

import com.devtritus.deusbase.api.ProgramArgs;
import com.devtritus.deusbase.node.client.NodeClient;
import com.devtritus.deusbase.node.env.NodeEnvironment;
import com.devtritus.deusbase.node.server.NodeServer;
import com.devtritus.deusbase.node.utils.NodeMode;

import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Node {
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final NodeClient nodeClient = new NodeClient();
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
        if(mode == NodeMode.MASTER) {

        } else if(mode == NodeMode.SLAVE) {
            String masterAddress = programArgs.get("master");
            String actualMasterUuid = nodeClient.doHandshake(masterAddress, env.getProperty("uuid"));
            String writtenMasterUuid = env.getProperty("masterUuid");
            if(writtenMasterUuid == null) { //first connection
                env.setProperty("masterUuid", actualMasterUuid);
            } else if(!actualMasterUuid.equals(writtenMasterUuid)) {
                throw new IllegalStateException(String.format("Slave isn't owned to master located at %s", masterAddress));
            }
        } else {
            throw new IllegalStateException();
        }

        nodeServer.start(mode, env, programArgs, Node::printBanner);
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
