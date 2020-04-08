package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.api.RequestBody;
import com.devtritus.deusbase.api.RequestBodyHandler;
import com.devtritus.deusbase.api.ResponseBody;
import com.devtritus.deusbase.api.WrongArgumentException;

public class MasterRequestHandler implements RequestBodyHandler {
    private MasterApi masterApi;
    private RequestBodyHandler nextHandler;

    public MasterRequestHandler(MasterApi masterApi) {
        this.masterApi = masterApi;
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
