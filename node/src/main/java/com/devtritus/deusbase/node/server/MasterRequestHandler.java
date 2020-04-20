package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.api.*;
import com.devtritus.deusbase.node.role.MasterApi;
import com.devtritus.deusbase.node.storage.RequestJournal;

import java.util.*;

public class MasterRequestHandler implements RequestBodyHandler {
    private final MasterApi masterApi;
    private final RequestJournal journal;
    private RequestBodyHandler nextHandler;

    public MasterRequestHandler(MasterApi masterApi, RequestJournal journal) {
        this.masterApi = masterApi;
        this.journal = journal;
    }

    @Override
    public NodeResponse handle(NodeRequest request) throws WrongArgumentException {
        String[] args = request.getArgs();
        if(request.getCommand() == Command.HANDSHAKE) {
            String masterUuid = masterApi.receiveSlaveHandshake(args[0], args[1]);
            NodeResponse response = new NodeResponse();
            Map<String, List<String>> result = new HashMap<>();
            result.put("result", Collections.singletonList(masterUuid));
            response.setData(result);

            return response;
        } else {
            journal.putRequest(request);
            NodeResponse result = nextHandler.handle(request);
            journal.flush(request);
            return result;
        }
    }

    @Override
    public void setNextHandler(RequestBodyHandler nextHandler) {
        this.nextHandler = nextHandler;
    }
}
