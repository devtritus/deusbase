package com.devtritus.deusbase.node.server;

import java.util.Map;

public class ServiceApi implements MasterApi, SlaveApi {
    @Override
    public String receiveSlaveHandshake(String slaveUuid) {
        return null;
    }

    @Override
    public void receiveLog(Map<String, byte[]> logPart) {

    }
}
