package com.devtritus.deusbase.node.role;

import java.util.List;

public interface MasterApi {
    List<String> receiveSlaveHandshake(String[] args);
}
