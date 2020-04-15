package com.devtritus.deusbase.node.role;

import java.util.Map;

public interface SlaveApi {
    void receiveLog(Map<String, byte[]> logPart);
}
