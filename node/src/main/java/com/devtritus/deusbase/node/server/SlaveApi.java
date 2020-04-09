package com.devtritus.deusbase.node.server;

import java.util.Map;

interface SlaveApi {
    void receiveLog(Map<String, byte[]> logPart);
}
