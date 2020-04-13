package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.api.RequestBody;
import com.devtritus.deusbase.api.RequestBodyHandler;
import com.devtritus.deusbase.api.ResponseBody;
import com.devtritus.deusbase.api.WrongArgumentException;

import java.util.*;

public class MasterRequestHandler implements RequestBodyHandler {
    private MasterApi masterApi;
    private RequestBodyHandler nextHandler;

    public MasterRequestHandler(MasterApi masterApi) {
        this.masterApi = masterApi;
    }

    @Override
    public ResponseBody handle(RequestBody requestBody) throws WrongArgumentException {
        String[] args = requestBody.getArgs();
        if(requestBody.getCommand().equals("handshake")) {
            String masterUuid = masterApi.receiveSlaveHandshake(args[0], args[1]);
            ResponseBody responseBody = new ResponseBody();
            Map<String, List<String>> result = new HashMap<>();
            result.put("result", Collections.singletonList(masterUuid));
            responseBody.setData(result);

            return responseBody;
        }
        return null;
    }

    @Override
    public void setNextHandler(RequestBodyHandler nextHandler) {
        this.nextHandler = nextHandler;
    }
}
