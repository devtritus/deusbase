package com.devtritus.deusbase.node.role;

import com.devtritus.deusbase.api.*;
import com.devtritus.deusbase.node.env.NodeEnvironment;
import com.devtritus.deusbase.node.server.NodeApiInitializer;
import org.apache.http.conn.HttpHostConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static com.devtritus.deusbase.node.utils.Utils.bytesToUtf8String;

public class SlaveNode implements SlaveApi {
    private final static Logger logger = LoggerFactory.getLogger(SlaveNode.class);

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private final NodeEnvironment env;
    private final int batchSize;
    private final NodeApiInitializer nodeApiInitializer;

    private boolean nodeInitialized;
    private boolean masterOnline;

    public SlaveNode(NodeEnvironment env, int batchSize, NodeApiInitializer nodeApiInitializer) {
        this.env = env;
        this.batchSize = batchSize;
        this.nodeApiInitializer = nodeApiInitializer;
    }

    public void init(String slaveAddress, String masterAddress) {
        final String masterAddress1 = "http://" + masterAddress;

        NodeClient client = new NodeClient(masterAddress1);

        executorService.scheduleAtFixedRate(() -> {
            if(heartbeat(client)) {
                if(!masterOnline) {
                    String state = env.getProperty("state");
                    if(!nodeInitialized && state != null && state.equals("connect")) {
                        nodeApiInitializer.init();
                        nodeInitialized = true;
                    }

                    handshake(state, slaveAddress, masterAddress1, client);
                    masterOnline = true;
                }
            } else {
                masterOnline = false;
            }
        }, 0, 10, TimeUnit.SECONDS);

    }

    @Override
    public List<NodeRequest> receiveLogBatch(ReadableByteChannel channel) {
        //TODO: make buffer less
        ByteBuffer buffer = ByteBuffer.allocate(batchSize);
        try {
            channel.read(buffer);
        } catch(Exception e)  {
            throw new RuntimeException(e);
        }

        buffer.flip();
        long batchId = buffer.getLong();
        env.putProperty("lastBatchId", Long.toString(batchId));

        List<NodeRequest> dd = new ArrayList<>();
        while(buffer.remaining() != 0) {
            int commandId = buffer.getInt();
            int argsCount = buffer.getInt();

            String[] args = new String[argsCount];
            for (int i = 0; i < argsCount; i++) {
                int argSize = buffer.getInt();
                byte[] argBytes = new byte[argSize];
                buffer.get(argBytes);
                String arg = bytesToUtf8String(argBytes);
                args[i] = arg;
            }

            Command command = Command.getCommandById(commandId);

            NodeRequest request = new NodeRequest(command, args);

            logger.debug("Received entry: {}", request);

            dd.add(request);
        }

        return dd;

    }

    private void handshake(String state, String slaveAddress, String masterAddress, NodeClient masterClient) {
        String slaveUuid = env.getPropertyOrThrowException("uuid");
        List<String> argsList = new ArrayList<>();
        argsList.add(slaveAddress);
        argsList.add(slaveUuid);

        if(state == null || state.equals("init")) {
            argsList.add("init");
        } else if(state.equals("connect")) {
            argsList.add("connect");
            String lastBatchId = env.getPropertyOrThrowException("lastBatchId");
            argsList.add(lastBatchId);
        }

        String[] args = argsList.toArray(new String[0]);

        NodeResponse response;
        try {
            response = masterClient.request(Command.HANDSHAKE, args);
        } catch (HttpHostConnectException e) {
            logger.error("Master node through address {} is unavailable. Only READ mode is permitted", masterAddress);
            return;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //TODO: handler response from master and run server
        String actualMasterUuid = response.getData().get("result").get(0);
        String writtenMasterUuid = env.getProperty("masterUuid");
        if(writtenMasterUuid == null) { //first connection
            env.putProperty("masterUuid", actualMasterUuid);
        } else if(!actualMasterUuid.equals(writtenMasterUuid)) {
            throw new IllegalStateException(String.format("Slave isn't owned to master located at %s", masterAddress));
        }

        logger.info("Handshake with master {} are completed", masterAddress);
    }

    @Override
    public void handleSyncComplete() {
        String state = env.getProperty("state");
        if(state == null || state.equals("init")) {
            env.putProperty("state", "connect");
            nodeApiInitializer.init();
            logger.info("Init slave's node api after init");
        }
        //TODO: connect state?
        logger.info("Sync is completed");
    }

    private boolean heartbeat(NodeClient client) {
        try {
            client.request(Command.HEARTBEAT);
            return true;
        } catch (Exception e) {
            logger.warn("Master node is unavailable: {}", e.getMessage());
            return false;
        }
    }
}
