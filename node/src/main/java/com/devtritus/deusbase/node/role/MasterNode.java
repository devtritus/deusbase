package com.devtritus.deusbase.node.role;

import com.devtritus.deusbase.node.env.NodeEnvironment;
import com.devtritus.deusbase.node.server.MasterApi;
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
    public String receiveSlaveHandshake(String slaveAddress, String slaveUuid) {
        slaves.put(slaveAddress, slaveUuid);
        return env.getPropertyOrThrowException("uuid");
    }

    private void uploadBatch() {
        System.out.println("UPLOAD");
    }
}
