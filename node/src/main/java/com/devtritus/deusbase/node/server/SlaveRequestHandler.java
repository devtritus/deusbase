package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.api.*;
import com.devtritus.deusbase.node.env.NodeEnvironment;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SlaveRequestHandler implements RequestHandler {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final NodeEnvironment env;

    private SlaveApi slaveApi;
    private NodeRequestHandler nextHandler;

    public SlaveRequestHandler(NodeEnvironment env) {
        this.env = env;
    }

    @Override
    public NodeResponse handle(Command command, ReadableByteChannel channel) throws Exception {
        NodeResponse nodeResponse = NodeResponse.ok();

        if (command == Command.HEARTBEAT) {
            //do nothing
        } else {
            Future<NodeResponse> future = executorService.submit(() ->
                    handleCriticalOperation(command, channel));

            return future.get();
        }

        return nodeResponse;
    }

    private NodeResponse handleCriticalOperation(Command command, ReadableByteChannel channel) throws Exception {
        if (command == Command.SYNC_COMPLETE) {
            slaveApi.handleSyncComplete();

        } else if (command == Command.BATCH) {
            List<NodeRequest> requests = slaveApi.parseLogBatch(channel);

            for (NodeRequest request : requests) {
                nextHandler.handle(request);
            }
        } else if (command == Command.COPY_INDEX) {
            slaveApi.copyDataToFile(channel, env.getIndexPath());

        } else if (command == Command.COPY_STORAGE) {
            slaveApi.copyDataToFile(channel, env.getStoragePath());

        } else if (command == Command.WRITE_PROPERTY) {
            RequestBody requestBody = JsonDataConverter.readNodeRequest(channel, RequestBody.class);
            String[] args = requestBody.getArgs();
            env.putProperty(args[0], args[1]);

        } else if (command.getType() == CommandType.WRITE) {
            throwSlaveException();

        } else {
            RequestBody requestBody = JsonDataConverter.readNodeRequest(channel, RequestBody.class);
            NodeRequest nodeRequest = new NodeRequest(command, requestBody.getArgs());
            return nextHandler.handle(nodeRequest);
        }

        return NodeResponse.ok();
    }

    public void setNextHandler(NodeRequestHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    public void setSlaveApi(SlaveApi slaveApi) {
        this.slaveApi = slaveApi;
    }

    private void throwSlaveException() {
        throw new IllegalStateException("Node has been running as SLAVE. Write operations are forbidden");
    }
}
