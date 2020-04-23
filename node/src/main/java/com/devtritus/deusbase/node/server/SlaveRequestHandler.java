package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.api.*;
import com.devtritus.deusbase.node.role.SlaveApi;
import java.nio.channels.ReadableByteChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlaveRequestHandler implements RequestHandler {
    private SlaveApi slaveApi;
    private NodeRequestHandler nextHandler;

    public void setNextHandler(NodeRequestHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    public SlaveRequestHandler(SlaveApi slaveApi) {
        this.slaveApi = slaveApi;
    }

    @Override
    public byte[] handle(Command command, ReadableByteChannel channel) throws WrongArgumentException {
        NodeResponse nodeResponse = new NodeResponse();
        if(command == Command.BATCH) {
            List<NodeRequest> requests = slaveApi.receiveLogBatch(channel);

            for(NodeRequest request1 : requests) {
                nextHandler.handle(request1);
            }

            Long batchId = slaveApi.getLastBatchId();
            Map<String, List<String>> result = new HashMap<>();
            result.put("batchId", Collections.singletonList(batchId.toString()));
            nodeResponse.setData(result);
        } else if(command.getType() == CommandType.WRITE) {
            throwSlaveException();
        } else {
            RequestBody requestBody = JsonDataConverter.readNodeRequest(channel, RequestBody.class);
            NodeRequest nodeRequest = new NodeRequest(command, requestBody.getArgs());
            nodeResponse = nextHandler.handle(nodeRequest);
        }

        return JsonDataConverter.convertObjectToJsonBytes(nodeResponse);
    }

    private void throwSlaveException() {
        throw new IllegalStateException("Node has been running as SLAVE. Write operations are forbidden");
    }
}
