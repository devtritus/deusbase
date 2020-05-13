package com.devtritus.deusbase.node.role;

import com.devtritus.deusbase.api.Command;
import com.devtritus.deusbase.api.NodeClient;
import com.devtritus.deusbase.api.NodeResponse;
import com.devtritus.deusbase.node.server.MasterApi;
import com.devtritus.deusbase.node.env.NodeEnvironment;
import com.devtritus.deusbase.node.server.NodeApiInitializer;
import com.devtritus.deusbase.node.storage.RequestJournal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MasterNode implements MasterApi {
    private final static Logger logger = LoggerFactory.getLogger(MasterNode.class);

    private final static int MAX_RETRY_COUNT = 3;
    private final static int MAX_NUMBER_OF_BATCHES_TO_UPLOAD = 5;
    private final static int UPLOAD_BATCH_COUNTER_LIMIT = 5;
    private final static int UPLOAD_BATCH_RATE_IN_SECONDS = 2;

    private final Map<String, SlaveParams> slaves = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final NodeEnvironment env;
    private final RequestJournal journal;
    private final NodeApiInitializer nodeApiInitializer;

    private int uploadBatchCounter;

    public MasterNode(NodeEnvironment env, RequestJournal journal, NodeApiInitializer nodeApiInitializer) {
        this.env = env;
        this.journal = journal;
        this.nodeApiInitializer = nodeApiInitializer;
    }

    public void init() {
        nodeApiInitializer.init();

        List<SlaveParams> slaves = readSlavesFromConfig();
        if(slaves == null) {
            slaves = new ArrayList<>();
            writeSlavesToConfig(slaves);
        }

        for(SlaveParams slave : slaves) {
            this.slaves.put(slave.getUuid(), slave);
        }

        scheduler.scheduleAtFixedRate(this::uploadBatch, 0, UPLOAD_BATCH_RATE_IN_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public List<String> receiveSlaveHandshake(String[] slaveArgs) {
        String masterUuid = env.getPropertyOrThrowException("uuid");

        String slaveAddress = "http://" + slaveArgs[0];
        String slaveUuid = slaveArgs[1];
        SlaveState slaveState = SlaveState.fromText(slaveArgs[2]);

        logger.info("Receive handshake from {}", slaveAddress);

        SlaveParams slave = new SlaveParams();
        slave.setUuid(slaveUuid);
        slave.setAddress(slaveAddress);

        NodeClient client = new NodeClient(slaveAddress);

        int journalSize = journal.size();
        int position = 0;
        if(slaveState == SlaveState.INIT) {
            doFullNodeCopy(client);
            logger.info("Init slave");
            position = journalSize;
        } else if(journalSize > 0 && slaveState == SlaveState.CONNECT) {
            Long nextSlaveBatchId = Long.parseLong(slaveArgs[3]) + 1;
            Long lastBatchId = journal.getLastBatchId();
            long firstBatchId = lastBatchId - journalSize + 1;
            if(firstBatchId <= nextSlaveBatchId && nextSlaveBatchId <= lastBatchId) {
                position = (int) (nextSlaveBatchId - firstBatchId);
                try {
                    position = sendBatch(client, position);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                doFullNodeCopy(client);
                position = journalSize;
                logger.info("Init forgotten slave");
            }
        }

        slave.setPosition(position);
        slaves.put(slaveUuid, slave);

        tryToRemoveBatches();

        slave.setClient(client);
        slave.setOnline(true);
        writeSlavesToConfig(slaves.values());

        try {
            client.request(Command.SYNC_COMPLETE);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        logger.info("Handshake with slave {} are completed", slaveAddress);

        return Collections.singletonList(masterUuid);
    }

    private void uploadBatch() {
        if(uploadBatchCounter++ < UPLOAD_BATCH_COUNTER_LIMIT && journal.size() < MAX_NUMBER_OF_BATCHES_TO_UPLOAD) {
            return;
        }

        uploadBatchCounter = 0;

        if (journal.isEmpty()) {
            logger.debug("Journal is empty");
            return;
        }

        List<SlaveParams> onlineSlaves = slaves.values().stream()
                .filter(SlaveParams::isOnline)
                .collect(Collectors.toList());

        if (onlineSlaves.isEmpty()) {
            logger.debug("There is no online slaves");
            return;
        }

        for (SlaveParams slave : onlineSlaves) {
            try {
                int nextPosition = sendBatch(slave.getClient(), slave.getPosition());
                slave.setPosition(nextPosition);
            } catch (IOException e) {
                logger.error("Batch was not sent to {}", slave.getAddress(), e);
                slave.setOnline(false);
            }
        }

        tryToRemoveBatches();

        writeSlavesToConfig(slaves.values());
    }

    private int sendBatch(NodeClient client, int position) throws IOException {
        int retryCount = 0;
        while(true) {
            try {
                return doSendBatch(client, position);
            } catch (IOException e) {
                retryCount++;
                logger.error("Batch was not sent to {}", client, e);
                if(retryCount < MAX_RETRY_COUNT) {
                    throw e;
                }
            }
        }
    }

    private int doSendBatch(NodeClient client, int startFromPosition) throws IOException {
        int nextPosition = 0;
        for(int i = startFromPosition; i < journal.size(); i++) {
            byte[] bytes = journal.getBatch(i);
            logger.debug("Send batch {} to {}", i, client);
            NodeResponse nodeResponse = client.streamRequest(Command.BATCH, new ByteArrayInputStream(bytes));
            nextPosition = i;
        }

        return nextPosition + 1;
    }

    private void doFullNodeCopy(NodeClient client) {
        try(InputStream inIndex = Files.newInputStream(env.getIndexPath(), StandardOpenOption.READ);
            InputStream inStorage = Files.newInputStream(env.getStoragePath(), StandardOpenOption.READ)) {
            client.streamRequest(Command.COPY_INDEX, inIndex);
            client.streamRequest(Command.COPY_STORAGE, inStorage);
            client.request(Command.WRITE_PROPERTY, "lastBatchId", Long.toString(journal.getLastBatchId()));
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<SlaveParams> readSlavesFromConfig() {
        return env.getArray("slaves", SlaveParams.class);
    }

    private void writeSlavesToConfig(Collection<SlaveParams> slaves) {
        env.putArray("slaves", new ArrayList<>(slaves));
    }

    private void tryToRemoveBatches() {
        int minPosition = slaves.values().stream()
                .mapToInt(SlaveParams::getPosition)
                .min()
                .orElseThrow(IllegalStateException::new);

        logger.debug("List of slaves: {}", slaves.values());

        if(minPosition > 0) {
            journal.removeBatches(minPosition);

            for(SlaveParams slave : slaves.values()) {
                slave.setPosition(slave.getPosition() - minPosition);
            }
        }
    }
}
