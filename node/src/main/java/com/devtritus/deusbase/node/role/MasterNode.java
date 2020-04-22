package com.devtritus.deusbase.node.role;

import com.devtritus.deusbase.node.env.NodeEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MasterNode implements MasterApi {
    private NodeEnvironment env;
    private Map<String, String> slaves = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public MasterNode(NodeEnvironment env) {
        this.env = env;
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

        if(slaveState.equals("init")) {

        } else if(slaveState.equals("connect")) {
            String slaveBatchIdString = slaveArgs[3];
            Long slaveBatchId = Long.parseLong(slaveBatchIdString);
        }
        //TODO: get address from request url
        slaves.put(slaveAddress, slaveUuid);
        return result;
    }

    private void uploadBatch() {
        System.out.println("UPLOAD");
    }
}
