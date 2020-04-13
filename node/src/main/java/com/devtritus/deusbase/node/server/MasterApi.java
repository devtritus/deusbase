package com.devtritus.deusbase.node.server;

public interface MasterApi {
    String receiveSlaveHandshake(String slaveAddress, String slaveUuid);
}
