package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.api.RequestBody;
import com.devtritus.deusbase.api.RequestBodyHandler;
import com.devtritus.deusbase.api.ResponseBody;
import com.devtritus.deusbase.api.WrongArgumentException;
import com.devtritus.deusbase.node.role.SlaveApi;

public class SlaveRequestHandler implements RequestBodyHandler {
    private SlaveApi slaveApi;
    private RequestBodyHandler nextHandler;

    public SlaveRequestHandler(SlaveApi slaveApi) {
        this.slaveApi = slaveApi;
    }

    @Override
    public ResponseBody handle(RequestBody requestBody) throws WrongArgumentException {
        return null;
    }

    @Override
    public void setNextHandler(RequestBodyHandler nextHandler) {
        this.nextHandler = nextHandler;
    }
}
