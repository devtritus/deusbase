package com.devtritus.deusbase.node.server;

import java.util.List;

public interface MasterApi {
    List<String> receiveSlaveHandshake(String[] args);
}
