package com.devtritus.deusbase.node.role;

import com.devtritus.deusbase.api.NodeRequest;

import java.nio.channels.ReadableByteChannel;
import java.util.List;

public interface SlaveApi {
    List<NodeRequest> parseLogBatch(ReadableByteChannel channel);
    void handleSyncComplete();
}
