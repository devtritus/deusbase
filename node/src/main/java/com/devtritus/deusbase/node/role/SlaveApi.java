package com.devtritus.deusbase.node.role;

import com.devtritus.deusbase.api.NodeRequest;

import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.util.List;

public interface SlaveApi {
    List<NodeRequest> parseLogBatch(ReadableByteChannel channel);
    void copyDataToFile(ReadableByteChannel channel, Path path);
    void handleSyncComplete();
}
