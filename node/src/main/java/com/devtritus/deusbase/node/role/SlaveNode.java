package com.devtritus.deusbase.node.role;

import com.devtritus.deusbase.api.*;
import com.devtritus.deusbase.node.env.NodeEnvironment;
import com.devtritus.deusbase.node.server.NodeApiInitializer;
import org.apache.http.conn.HttpHostConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static com.devtritus.deusbase.node.utils.Utils.bytesToUtf8String;

public class SlaveNode implements SlaveApi {
    private final static Logger logger = LoggerFactory.getLogger(SlaveNode.class);

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private final NodeEnvironment env;
    private final NodeApiInitializer nodeApiInitializer;

    private SlaveState state;
    private boolean nodeInitialized;
    private boolean masterOnline;

    public SlaveNode(NodeEnvironment env, NodeApiInitializer nodeApiInitializer) {
        this.env = env;
        this.nodeApiInitializer = nodeApiInitializer;
    }

    public void init(String slaveAddress, String masterAddress) {
        NodeClient client = new NodeClient(masterAddress);

        executorService.scheduleAtFixedRate(() -> {
            if(heartbeat(client)) {
                if(!masterOnline) {
                    String stateText = env.getProperty("state");
                    state = stateText != null ? SlaveState.fromText(stateText) : SlaveState.INIT;
                    if(!nodeInitialized && state == SlaveState.CONNECT) {
                        nodeApiInitializer.init();
                        nodeInitialized = true;
                    }

                    handshake(state, slaveAddress, masterAddress, client);
                    masterOnline = true;
                }
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    @Override
    public List<NodeRequest> parseLogBatch(ReadableByteChannel channel) {
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            while (channel.read(buffer) != -1) {
                buffer.flip();

                while(buffer.hasRemaining()) {
                    out.write(buffer.get());
                }

                buffer.clear();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ByteBuffer batch = ByteBuffer.wrap(out.toByteArray());

        long batchId = batch.getLong();
        env.putProperty("lastBatchId", Long.toString(batchId));

        List<NodeRequest> requests = new ArrayList<>();
        while(batch.remaining() != 0) {
            int commandId = batch.getInt();
            int argsCount = batch.getInt();

            String[] args = new String[argsCount];
            for (int i = 0; i < argsCount; i++) {
                int argSize = batch.getInt();
                byte[] argBytes = new byte[argSize];
                batch.get(argBytes);
                String arg = bytesToUtf8String(argBytes);
                args[i] = arg;
            }

            Command command = Command.getCommandById(commandId);

            NodeRequest request = new NodeRequest(command, args);

            logger.debug("Received entry: {}", request);

            requests.add(request);
        }

        return requests;
    }

    private void handshake(SlaveState state, String slaveAddress, String masterAddress, NodeClient masterClient) {
        String slaveUuid = env.getPropertyOrThrowException("uuid");
        List<String> argsList = new ArrayList<>();

        argsList.add(slaveAddress);
        argsList.add(slaveUuid);
        argsList.add(state.getText());

        if(state == SlaveState.CONNECT) {
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
        if(state == SlaveState.INIT) {
            env.putProperty("state", SlaveState.CONNECT.getText());
            nodeApiInitializer.init();
            logger.info("Init slave's node api after init");
        }
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
