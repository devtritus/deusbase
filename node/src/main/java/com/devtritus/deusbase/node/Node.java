package com.devtritus.deusbase.node;

import com.devtritus.deusbase.api.NodeRequest;
import com.devtritus.deusbase.api.ProgramArgs;
import com.devtritus.deusbase.node.server.NodeApi;
import com.devtritus.deusbase.node.env.NodeEnvironment;
import com.devtritus.deusbase.node.role.MasterNode;
import com.devtritus.deusbase.node.role.SlaveNode;
import com.devtritus.deusbase.node.server.*;
import com.devtritus.deusbase.node.storage.FlushContext;
import com.devtritus.deusbase.node.storage.RequestJournal;
import com.devtritus.deusbase.node.utils.NodeMode;
import com.devtritus.deusbase.node.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

import static com.devtritus.deusbase.api.ProgramArgNames.*;
import static com.devtritus.deusbase.node.env.NodeSettings.*;
import static com.devtritus.deusbase.node.utils.Utils.isEmptyFile;

//TODO: create template method for all modes
class Node {
    private final static Logger logger = LoggerFactory.getLogger(Router.class);

    private NodeMode mode;
    private ProgramArgs programArgs;

    Node(NodeMode mode, ProgramArgs programArgs) {
        this.mode = mode;
        this.programArgs = programArgs;
    }

    void start() {
        final String host = programArgs.getOrDefault(HOST, DEFAULT_HOST);
        final int port = programArgs.getIntegerOrDefault(PORT, DEFAULT_PORT);
        final int treeM = programArgs.getIntegerOrDefault(TREE_M, DEFAULT_TREE_M);
        final int treeCacheLimit = programArgs.getIntegerOrDefault(TREE_CACHE_LIMIIT, DEFAULT_TREE_CACHE_LIMIT);

        final String nodeAddress = host + ":" + port;

        final NodeEnvironment env = new NodeEnvironment();
        env.setUp(programArgs);

        String uuid = env.getProperty("uuid");
        if(uuid == null) {
            uuid = UUID.randomUUID().toString();
            env.putProperty("uuid", uuid);
        }

        final NodeApi nodeApi = new NodeApi();
        final NodeApiInitializer nodeApiInitializer = new NodeApiInitializer(treeM, treeCacheLimit, env.getIndexPath(), env.getStoragePath(), nodeApi);
        final CrudRequestHandler crudRequestHandler = new CrudRequestHandler();

        crudRequestHandler.setApi(nodeApi);

        if(mode == NodeMode.MASTER) {
            int journalBatchSize = programArgs.getIntegerOrDefault(JOURNAL_BATCH_SIZE, DEFAULT_JOURNAL_BATCH_SIZE);
            int journalMinSizeToTruncate = programArgs.getIntegerOrDefault(JOURNAL_MIN_SIZE_TO_TRUNCATE, DEFAULT_JOURNAL_MIN_SIZE_TO_TRUNCATE);
            String pathToJournal = programArgs.getOrDefault(JOURNAL_PATH, DEFAULT_JOURNAL_PATH);
            String pathToFlushContext = programArgs.getOrDefault(FLUSH_CONTEXT_PATH, DEFAULT_FLUSH_CONTEXT_PATH);

            Path journalPath = env.createOrGetNodeFile(pathToJournal);
            Path flushContextPath = env.createOrGetNodeFile(pathToFlushContext);

            FlushContext flushContext = new FlushContext(flushContextPath);
            if(!isEmptyFile(flushContextPath)) {
                List<NodeRequest> unflushedRequests = flushContext.getAll();
                if(!unflushedRequests.isEmpty()) {
                    throw new IllegalStateException("Unflushed requests were found, to get details see " + flushContextPath.toAbsolutePath().normalize());
                }
            }

            RequestJournal journal = RequestJournal.init(journalPath, journalBatchSize, journalMinSizeToTruncate);
            MasterRequestHandler requestHandler = new MasterRequestHandler(journal, flushContext);
            HttpRequestHandler httpRequestHandler = new HttpRequestHandler(requestHandler, Executors.newSingleThreadExecutor()); //use single-thread executor to ensure thread safe
            NodeServer nodeServer = new NodeServer(host, port, httpRequestHandler, () -> successCallback(mode));
            MasterNode masterNode = new MasterNode(env, journal, nodeApiInitializer);

            requestHandler.setNextHandler(crudRequestHandler);
            requestHandler.setMasterApi(masterNode);

            nodeServer.start();
            masterNode.init();

        } else if(mode == NodeMode.SLAVE) {
            String masterAddress = programArgs.get(MASTER_ADDRESS);

            SlaveRequestHandler requestHandler = new SlaveRequestHandler(env);
            HttpRequestHandler httpRequestHandler = new HttpRequestHandler(requestHandler, Executors.newSingleThreadExecutor()); //use single-thread executor to ensure thread safe
            NodeServer nodeServer = new NodeServer(host, port, httpRequestHandler, () -> successCallback(mode));
            SlaveNode slaveNode = new SlaveNode(env, nodeApiInitializer);

            requestHandler.setNextHandler(crudRequestHandler);
            requestHandler.setSlaveApi(slaveNode);

            nodeServer.start();

            slaveNode.init(nodeAddress, masterAddress);
        } else {
            throw new IllegalStateException(String.format("Unexpected mode: %s", mode));
        }
    }

    private static void successCallback(NodeMode mode) {
        Utils.printFromFile("banner.txt");
        logger.info("Mode - {}", mode.name().toLowerCase());
    }
}
