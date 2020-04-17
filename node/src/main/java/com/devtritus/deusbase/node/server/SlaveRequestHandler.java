package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.api.NodeRequest;
import com.devtritus.deusbase.api.RequestBodyHandler;
import com.devtritus.deusbase.api.NodeResponse;
import com.devtritus.deusbase.api.WrongArgumentException;
import com.devtritus.deusbase.node.role.SlaveApi;

public class SlaveRequestHandler implements RequestBodyHandler {
    private SlaveApi slaveApi;
    private RequestBodyHandler nextHandler;

    public SlaveRequestHandler(SlaveApi slaveApi) {
        this.slaveApi = slaveApi;
    }

    @Override
    public NodeResponse handle(NodeRequest request) throws WrongArgumentException {
        return nextHandler.handle(request);
    }

    @Override
    public void setNextHandler(RequestBodyHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    private void throwSlaveException() {
        throw new IllegalStateException("Node has been running as SLAVE. Write operations are forbidden");
    }
}
