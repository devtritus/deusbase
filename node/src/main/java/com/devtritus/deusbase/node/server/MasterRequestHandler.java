package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.api.*;
import com.devtritus.deusbase.node.role.MasterApi;
import com.devtritus.deusbase.node.storage.RequestJournal;

import java.nio.channels.ReadableByteChannel;
import java.util.*;

public class MasterRequestHandler implements RequestHandler {
    private final MasterApi masterApi;
    private final RequestJournal journal;

    public void setNextHandler(NodeRequestHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    private NodeRequestHandler nextHandler;

    public MasterRequestHandler(MasterApi masterApi, RequestJournal journal) {
        this.masterApi = masterApi;
        this.journal = journal;
    }

    @Override
    public byte[] handle(Command command, ReadableByteChannel channel) {
        NodeResponse nodeResponse;
        if(command == Command.HANDSHAKE) {
            RequestBody requestBody = JsonDataConverter.readNodeRequest(channel, RequestBody.class);
            List<String> responsesValues = masterApi.receiveSlaveHandshake(requestBody.getArgs());
            Map<String, List<String>> result = new HashMap<>();
            result.put("result", responsesValues);
            nodeResponse = new NodeResponse();
            nodeResponse.setData(result);
            nodeResponse.setCode(ResponseStatus.OK.getCode());
        } else {
            RequestBody requestBody = JsonDataConverter.readNodeRequest(channel, RequestBody.class);
            NodeRequest nodeRequest = new NodeRequest(command, requestBody.getArgs());
            if(command.getType() == CommandType.WRITE) {
                journal.putRequest(nodeRequest);
            }

            nodeResponse = nextHandler.handle(nodeRequest);

            if(command.getType() == CommandType.WRITE) {
                journal.flush(nodeRequest);
            }
        }
        return JsonDataConverter.convertObjectToJsonBytes(nodeResponse);
    }
}
