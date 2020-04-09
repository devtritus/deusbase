package com.devtritus.deusbase.node.server;

interface MasterApi {
    String receiveSlaveHandshake(String slaveUuid);
}
