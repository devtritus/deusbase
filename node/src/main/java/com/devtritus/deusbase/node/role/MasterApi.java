package com.devtritus.deusbase.node.role;

public interface MasterApi {
    String receiveSlaveHandshake(String slaveAddress, String slaveUuid);
}
