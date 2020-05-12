package com.devtritus.deusbase.node.api;

import java.util.List;

public interface MasterApi {
    List<String> receiveSlaveHandshake(String[] args);
}
