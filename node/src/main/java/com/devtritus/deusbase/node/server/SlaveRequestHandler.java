package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.api.*;
import com.devtritus.deusbase.node.role.SlaveApi;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlaveRequestHandler implements RequestBodyHandler {
    private SlaveApi slaveApi;
    private RequestBodyHandler nextHandler;

    public SlaveRequestHandler(SlaveApi slaveApi) {
        this.slaveApi = slaveApi;
    }

    @Override
    public NodeResponse handle(NodeRequest request) throws WrongArgumentException {
        //TODO:
        if(request.getCommand() == Command.BATCH) {
            List<NodeRequest> requests = slaveApi.receiveLogBatch(new byte[0]);

            for(NodeRequest request1 : requests) {
                nextHandler.handle(request1);
            }

            Long batchId = slaveApi.getLastBatchId();
            NodeResponse response = new NodeResponse();
            Map<String, List<String>> result = new HashMap<>();
            result.put("batchId", Collections.singletonList(batchId.toString()));
            response.setData(result);
            return response;
        } else if(request.getCommand().getType() == CommandType.WRITE) {
            throwSlaveException();
            return null;
        } else {
            return nextHandler.handle(request);
        }
    }

    @Override
    public void setNextHandler(RequestBodyHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    private void throwSlaveException() {
        throw new IllegalStateException("Node has been running as SLAVE. Write operations are forbidden");
    }
}
