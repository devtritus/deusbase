package com.devtritus.deusbase.node.role;

import com.devtritus.deusbase.api.Command;
import com.devtritus.deusbase.api.NodeClient;
import com.devtritus.deusbase.api.NodeResponse;
import com.devtritus.deusbase.node.env.NodeEnvironment;
import com.devtritus.deusbase.node.storage.RequestJournal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MasterNode implements MasterApi {
    private final static Logger logger = LoggerFactory.getLogger(MasterNode.class);

    private NodeEnvironment env;
    private Map<String, SlaveStatus> slaves = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final RequestJournal journal;

    public MasterNode(NodeEnvironment env, RequestJournal journal) {
        this.env = env;
        this.journal = journal;
    }

    public void init() {
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

        SlaveStatus slaveStatus = new SlaveStatus();
        slaveStatus.address = "http://" + slaveAddress;
        int position = 0;
        if(slaveState.equals("init")) {
            if(journal.size() != 0) {
                //do full copy
            }
        } else if(slaveState.equals("connect")) {
            String slaveBatchIdString = slaveArgs[3];
            Long slaveBatchId = Long.parseLong(slaveBatchIdString);
            Long firstBatchId = journal.getLastBatchId();
            if(slaveBatchId >= firstBatchId && slaveBatchId < firstBatchId + journal.size()) {
                position = (int)(slaveBatchId - firstBatchId);
                int successfullySentBatchesCount = sendBatch(slaveAddress, position);
                position += successfullySentBatchesCount;
            } else {
                // do full copy
            }
        }
        //TODO: get address from request url
        slaveStatus.position = position;
        slaves.put(slaveUuid, slaveStatus);
        return result;
    }

    private void uploadBatch() {
        //TODO: add slaveList.json
        for(SlaveStatus slave : slaves.values()) {
            int successfullySentBatchesCount = sendBatch(slave.address, slave.position);
            slave.position += successfullySentBatchesCount;
        }

        int minPosition = slaves.values().stream()
                .mapToInt(slave -> slave.position)
                .min()
                .orElse(0);

        for(int i = 0; i < minPosition; i++) {
            //TODO: add function in journal
            journal.removeFirstRequestsBatch();
        }
    }

    private int sendBatch(String slaveAddress, int startFromPosition) {
        int successfullySentBatchesCount = 0;
        try {
            for(int i = startFromPosition; i < journal.size(); i++) {
                byte[] bytes = journal.getBatch(i);
                NodeClient client = new NodeClient(slaveAddress);
                NodeResponse nodeResponse = client.streamRequest(Command.BATCH, new ByteArrayInputStream(bytes));
                successfullySentBatchesCount++;
            }
        } catch (Exception e) {
            logger.error("Batch wasn't sent to {}", slaveAddress, e);
        }

        return successfullySentBatchesCount;
    }

    private static class SlaveStatus {
        private String address;
        private int position;
    }
}
