package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.api.*;
import com.devtritus.deusbase.node.role.MasterApi;
import com.devtritus.deusbase.node.storage.RequestJournal;

import java.nio.channels.ReadableByteChannel;
import java.util.*;

public class MasterRequestHandler implements RequestHandler {
    private final RequestJournal journal;

    private MasterApi masterApi;

    public void setNextHandler(NodeRequestHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    private NodeRequestHandler nextHandler;

    public MasterRequestHandler(RequestJournal journal) {
        this.journal = journal;
    }

    @Override
    public byte[] handle(Command command, ReadableByteChannel channel) {
        NodeResponse nodeResponse = new NodeResponse();;
        if(command == Command.HANDSHAKE) {
            RequestBody requestBody = JsonDataConverter.readNodeRequest(channel, RequestBody.class);
            List<String> responsesValues = masterApi.receiveSlaveHandshake(requestBody.getArgs());
            Map<String, List<String>> result = new HashMap<>();
            result.put("result", responsesValues);
            nodeResponse.setData(result);
            nodeResponse.setCode(ResponseStatus.OK.getCode());
        } else if(command == Command.HEARTBEAT) {
            //do nothing
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

    public void setMasterApi(MasterApi masterApi) {
        this.masterApi = masterApi;
    }
}
