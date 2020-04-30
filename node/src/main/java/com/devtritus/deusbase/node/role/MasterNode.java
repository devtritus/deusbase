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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
        String slaveAddress = slaveArgs[0];
        String slaveUuid = slaveArgs[1];
        String slaveState = slaveArgs[2];

        List<String> result = new ArrayList<>();
        String masterUuid = env.getPropertyOrThrowException("uuid");
        result.add(masterUuid);

        SlaveParams slaveStatus = new SlaveParams();
        slaveStatus.setUuid(slaveUuid);
        slaveStatus.setAddress("http://" + slaveAddress);
        int journalSize = journal.size();
        int position = 0;
        if(slaveState.equals("init")) {
            doFullNodeCopy("http://" + slaveAddress);
            logger.info("Init");
        } else if(slaveState.equals("connect")) {
            Long slaveBatchId = Long.parseLong(slaveArgs[3]);
            Long lastBatchId = journal.getLastBatchId();
            if (lastBatchId != -1) {
                long firstBatchId = lastBatchId - journalSize;
                logger.info("First batch id = {}, slave batch id = {}, last batch id = {}", firstBatchId, slaveBatchId, lastBatchId);
                if(firstBatchId < slaveBatchId && slaveBatchId <= lastBatchId) {
                    position = (int) (slaveBatchId - lastBatchId);
                    try {
                        int successfullySentBatchesCount = sendBatch("http://" + slaveAddress, position);
                        position += successfullySentBatchesCount;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else{
                    doFullNodeCopy("http://" + slaveAddress);
                    logger.info("Init forgotten slave");
                }
            }
            //TODO: get address from request url
            slaveStatus.setPosition(position);
            slaves.put(slaveUuid, slaveStatus);
            writeSlavesToConfig(slaves.values());

            slaveStatus.setOnline(true);
            logger.info("Slave {} is online after 'connect' state", slaveAddress);
        }

        try {
            NodeClient client = new NodeClient("http://" + slaveAddress);
            client.request(Command.SYNC_COMPLETE);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        return result;
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
        if(journal.isEmpty()) {
            return;
        }

        List<SlaveParams> onlineSlaves = slaves.values().stream()
                .filter(SlaveParams::isOnline)
                .collect(Collectors.toList());

        if(onlineSlaves.isEmpty()) {
            return;
        }

        for(SlaveParams slave : onlineSlaves) {
            int successfullySentBatchesCount = 0;
            try {
                successfullySentBatchesCount = sendBatch(slave.getAddress(), slave.getPosition());
            } catch (Exception e) {
                logger.error("Batch wasn't sent to {}", slave.getAddress(), e);
                slave.setOnline(false); //TODO: set offline state only after a few retries
            }
            slave.setPosition(slave.getPosition() + successfullySentBatchesCount);
        }

        int minPosition = slaves.values().stream()
                .mapToInt(SlaveParams::getPosition)
                .min()
                .orElse(0);

        journal.removeBatches(minPosition + 1);

        writeSlavesToConfig(slaves.values());
    }

    private int sendBatch(String slaveAddress, int startFromPosition) throws Exception {
        int successfullySentBatchesCount = 0;
        NodeClient client = new NodeClient(slaveAddress);

        for(int i = startFromPosition; i < journal.size(); i++) {
            byte[] bytes = journal.getBatch(i);
            logger.info("Send batch {} to {}", i, slaveAddress);
            NodeResponse nodeResponse = client.streamRequest(Command.BATCH, new ByteArrayInputStream(bytes));
            successfullySentBatchesCount++;
        }

        return successfullySentBatchesCount;
    }

    private void doFullNodeCopy(String slaveAddress) {
        try(InputStream inIndex = Files.newInputStream(env.getIndexPath(), StandardOpenOption.READ);
            InputStream inStorage = Files.newInputStream(env.getStoragePath(), StandardOpenOption.READ)) {
            NodeClient client = new NodeClient(slaveAddress);
            client.streamRequest(Command.COPY_INDEX, inIndex);
            client.streamRequest(Command.COPY_STORAGE, inStorage);
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
}
