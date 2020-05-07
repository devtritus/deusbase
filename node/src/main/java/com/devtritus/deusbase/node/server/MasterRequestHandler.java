package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.api.*;
import com.devtritus.deusbase.node.role.MasterApi;
import com.devtritus.deusbase.node.storage.RequestJournal;

import java.nio.channels.ReadableByteChannel;
import java.util.*;

public class MasterRequestHandler implements RequestHandler {
    private final RequestJournal journal;

    private MasterApi masterApi;
    private NodeRequestHandler nextHandler;

    public MasterRequestHandler(RequestJournal journal) {
        this.journal = journal;
    }

    @Override
    public NodeResponse handle(Command command, ReadableByteChannel channel) {
        NodeResponse nodeResponse = NodeResponse.ok();
        if(command == Command.HANDSHAKE) {
            RequestBody requestBody = JsonDataConverter.readNodeRequest(channel, RequestBody.class);
            List<String> values = masterApi.receiveSlaveHandshake(requestBody.getArgs());
            nodeResponse.setData("result", values);
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

        return nodeResponse;
    }

    public void setMasterApi(MasterApi masterApi) {
        this.masterApi = masterApi;
    }

    public void setNextHandler(NodeRequestHandler nextHandler) {
        this.nextHandler = nextHandler;
    }
}
