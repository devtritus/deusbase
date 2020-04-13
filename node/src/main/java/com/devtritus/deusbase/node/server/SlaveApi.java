package com.devtritus.deusbase.node.server;

import java.util.Map;

public interface SlaveApi {
    void receiveLog(Map<String, byte[]> logPart);
}
