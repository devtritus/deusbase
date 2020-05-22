package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.api.*;
import com.devtritus.deusbase.node.storage.FlushContext;
import com.devtritus.deusbase.node.storage.RequestJournal;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.util.*;

public class MasterRequestHandler implements RequestHandler {
    private final RequestJournal journal;

    private MasterApi masterApi;
    private NodeRequestHandler nextHandler;
    private FlushContext flushContext;

    public MasterRequestHandler(RequestJournal journal, FlushContext flushContext) {
        this.journal = journal;
        this.flushContext = flushContext;
    }

    @Override
    public NodeResponse handle(Command command, ReadableByteChannel channel) throws IOException, UnhandledCommandException {
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
                flushContext.put(nodeRequest);
            }

            try {
                nodeResponse = nextHandler.handle(nodeRequest);
            } catch(Exception e) {
                flushContext.remove(nodeRequest);
                throw e;
            }

            if(command.getType() == CommandType.WRITE) {
                flushContext.remove(nodeRequest);
                synchronized (journal) {
                    journal.putRequest(nodeRequest);
                }
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
