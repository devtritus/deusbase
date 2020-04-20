package com.devtritus.deusbase.node;

import com.devtritus.deusbase.api.NodeRequest;
import com.devtritus.deusbase.api.ProgramArgs;
import com.devtritus.deusbase.api.RequestBodyHandler;
import com.devtritus.deusbase.node.env.NodeEnvironment;
import com.devtritus.deusbase.node.role.MasterNode;
import com.devtritus.deusbase.node.role.SlaveNode;
import com.devtritus.deusbase.node.server.*;
import com.devtritus.deusbase.node.storage.FlushContext;
import com.devtritus.deusbase.node.storage.RequestJournal;
import com.devtritus.deusbase.node.utils.NodeMode;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import static com.devtritus.deusbase.api.ProgramArgNames.*;
import static com.devtritus.deusbase.node.env.NodeSettings.*;
import static com.devtritus.deusbase.node.utils.Utils.createFileIfNotExist;
import static com.devtritus.deusbase.node.utils.Utils.isEmptyFile;

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

        CrudRequestHandler crudRequestHandler = new CrudRequestHandler(env.getNodeApi());

        RequestBodyHandler requestBodyHandler;

        if(mode == NodeMode.MASTER) {
            int journalBatchSize = programArgs.getIntegerOrDefault(JOURNAL_BATCH_SIZE, DEFAULT_JOURNAL_BATCH_SIZE);
            int journalMinSizeToTruncate = programArgs.getIntegerOrDefault(JOURNAL_MIN_SIZE_TO_TRUNCATE, DEFAULT_JOURNAL_MIN_SIZE_TO_TRUNCATE);

            String pathToJournal = programArgs.getOrDefault(JOURNAL_PATH, DEFAULT_JOURNAL_PATH);
            String pathToFlushContext = programArgs.getOrDefault(FLUSH_CONTEXT_PATH, DEFAULT_FLUSH_CONTEXT_PATH);

            Path journalPath = Paths.get(pathToJournal);
            Path flushContextPath = Paths.get(pathToFlushContext);

            createFileIfNotExist(journalPath);
            createFileIfNotExist(flushContextPath);

            FlushContext flushContext = new FlushContext(flushContextPath);
            if(!isEmptyFile(flushContextPath)) {
                List<NodeRequest> unflushedRequests = flushContext.getAll();
                if(!unflushedRequests.isEmpty()) {
                    throw new IllegalStateException("Unflushed requests were found");
                }
            }

            RequestJournal journal = RequestJournal.init(journalPath, flushContext, journalBatchSize, journalMinSizeToTruncate);
            MasterNode masterNode = new MasterNode(env);

            requestBodyHandler = new MasterRequestHandler(masterNode, journal);
            masterNode.init();

        } else if(mode == NodeMode.SLAVE) {
            String masterAddress = programArgs.get(MASTER_ADDRESS);
            SlaveNode slaveNode = new SlaveNode(env);

            requestBodyHandler = new SlaveRequestHandler(slaveNode);
            slaveNode.init(nodeAddress, masterAddress);

        } else {
            throw new IllegalStateException(String.format("Unexpected mode: %s", mode));
        }

        requestBodyHandler.setNextHandler(crudRequestHandler);

        nodeServer.start(host, port, requestBodyHandler, Node::printBanner);
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
