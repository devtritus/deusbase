package com.devtritus.deusbase.node.role;

import com.devtritus.deusbase.api.NodeRequest;

import java.util.List;

public interface SlaveApi {
    List<NodeRequest> receiveLogBatch(byte[] bytes);
    Long getLastBatchId();
}
