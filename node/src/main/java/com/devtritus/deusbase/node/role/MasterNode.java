package com.devtritus.deusbase.node.role;

import com.devtritus.deusbase.api.Command;
import com.devtritus.deusbase.api.NodeClient;
import com.devtritus.deusbase.api.NodeResponse;
import com.devtritus.deusbase.node.env.NodeEnvironment;
import com.devtritus.deusbase.node.server.NodeApiInitializer;
import com.devtritus.deusbase.node.storage.RequestJournal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ByteArrayInputStream;
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

    private final Map<String, SlaveParams> slaves = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final NodeEnvironment env;
    private final RequestJournal journal;
    private final NodeApiInitializer nodeApiInitializer;

    public MasterNode(NodeEnvironment env, RequestJournal journal, NodeApiInitializer nodeApiInitializer) {
        this.env = env;
        this.journal = journal;
        this.nodeApiInitializer = nodeApiInitializer;
    }

    public void init() {
        nodeApiInitializer.init();

        List<SlaveParams> slaveStatuses = readSlavesFromConfig();
        if(slaveStatuses == null) {
            slaveStatuses = new ArrayList<>();
            writeSlavesToConfig(slaveStatuses);
        }

        for(SlaveParams slaveStatus : slaveStatuses) {
            slaves.put(slaveStatus.getUuid(), slaveStatus);
        }

        scheduler.scheduleAtFixedRate(this::uploadBatch, 0, 20, TimeUnit.SECONDS);
    }

    @Override
    public List<String> receiveSlaveHandshake(String[] slaveArgs) {
        String masterUuid = env.getPropertyOrThrowException("uuid");

        String slaveAddress = slaveArgs[0];
        String slaveUuid = slaveArgs[1];
        String slaveState = slaveArgs[2];

        SlaveParams slaveStatus = new SlaveParams();
        slaveStatus.setUuid(slaveUuid);
        slaveStatus.setAddress("http://" + slaveAddress);
        int journalSize = journal.size();
        int position = 0;
        if(slaveState.equals("init")) {
            doFullNodeCopy("http://" + slaveAddress);
            logger.info("Init");
        } else if(journalSize > 0 && slaveState.equals("connect")) {
            Long slaveBatchId = Long.parseLong(slaveArgs[3]);
            Long lastBatchId = journal.getLastBatchId();
            long firstBatchId = lastBatchId - journalSize + 1;
            logger.info("First batch id = {}, slave batch id = {}, last batch id = {}", firstBatchId, slaveBatchId, lastBatchId);
            if(firstBatchId <= slaveBatchId && slaveBatchId <= lastBatchId) {
                position = (int) (slaveBatchId - firstBatchId);
                try {
                    int lastSentBatchPosition = sendBatch("http://" + slaveAddress, position);
                    position = lastSentBatchPosition;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                doFullNodeCopy("http://" + slaveAddress);
                logger.info("Init forgotten slave");
            }
            logger.info("Slave {} is online after 'connect' state", slaveAddress);
        }

        //TODO: get address from request url
        slaveStatus.setPosition(position);
        slaves.put(slaveUuid, slaveStatus);

        tryToRemoveBatches();

        writeSlavesToConfig(slaves.values());

        slaveStatus.setOnline(true);

        try {
            NodeClient client = new NodeClient("http://" + slaveAddress);
            client.request(Command.SYNC_COMPLETE);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        logger.info("Handshake with slave {} are completed", slaveAddress);

        return Collections.singletonList(masterUuid);
    }

    @Override
    public void copyFinished(String[] slaveArgs) {
        String slaveAddress = slaveArgs[0];
        String slaveUuid = slaveArgs[1];

        SlaveParams slaveStatus = new SlaveParams();
        slaveStatus.setUuid(slaveUuid);
        slaveStatus.setAddress("http://" + slaveAddress);

        slaveStatus.setPosition(0);
        slaveStatus.setOnline(true);
        slaves.put(slaveUuid, slaveStatus);
        writeSlavesToConfig(slaves.values());
        logger.info("Slave {} is online after 'init' state", slaveAddress);
    }

    private void uploadBatch() {
        try {
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
                int nextPosition = slave.getPosition();
                try {
                    nextPosition = sendBatch(slave.getAddress(), slave.getPosition());
                } catch (Exception e) {
                    logger.error("Batch wasn't sent to {}", slave.getAddress(), e);
                    slave.setOnline(false); //TODO: set offline state only after a few retries
                }
                slave.setPosition(nextPosition);
            }

            tryToRemoveBatches();

            writeSlavesToConfig(slaves.values());
        } catch (Exception e) {
            logger.error("Replication error", e);
        }
    }

    private int sendBatch(String slaveAddress, int startFromPosition) throws Exception {
        NodeClient client = new NodeClient(slaveAddress);

        int nextPosition;
        for(nextPosition = startFromPosition; nextPosition < journal.size(); nextPosition++) {
            byte[] bytes = journal.getBatch(nextPosition);
            logger.debug("Send batch {} to {}", nextPosition, slaveAddress);
            NodeResponse nodeResponse = client.streamRequest(Command.BATCH, new ByteArrayInputStream(bytes));
        }

        return nextPosition == 0 ? 0 : nextPosition + 1;
    }

    private void doFullNodeCopy(String slaveAddress) {
        try(InputStream inIndex = Files.newInputStream(env.getIndexPath(), StandardOpenOption.READ);
            InputStream inStorage = Files.newInputStream(env.getStoragePath(), StandardOpenOption.READ)) {
            NodeClient client = new NodeClient(slaveAddress);
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

        OptionalInt offlineSlaveMinPosition = slaves.values().stream()
                .filter(slave -> !slave.isOnline())
                .mapToInt(SlaveParams::getPosition)
                .min();

        if(offlineSlaveMinPosition.isPresent()) {
            minPosition = Math.min(minPosition, offlineSlaveMinPosition.getAsInt());
        }

        if(minPosition > 0) {
            journal.removeBatches(minPosition);

            for(SlaveParams slave : slaves.values()) {
                slave.setPosition(slave.getPosition() - minPosition);
            }
        }
    }
}
