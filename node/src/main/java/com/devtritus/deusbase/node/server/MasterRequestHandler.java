package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.api.*;
import com.devtritus.deusbase.node.storage.FlushContext;
import com.devtritus.deusbase.node.storage.RequestJournal;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MasterRequestHandler implements RequestHandler {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final RequestJournal journal;

    private MasterApi masterApi;
    private NodeRequestHandler nextHandler;
    private FlushContext flushContext;

    public MasterRequestHandler(RequestJournal journal, FlushContext flushContext) {
        this.journal = journal;
        this.flushContext = flushContext;
    }

    @Override
    public NodeResponse handle(Command command, ReadableByteChannel channel) throws Exception {
        final NodeResponse nodeResponse = NodeResponse.ok();

        if(command == Command.HEARTBEAT) {
            //do nothing
        } else {
            Future<NodeResponse> future = executorService.submit(() -> handleCriticalOperation(command, channel));
            return future.get();
        }

        return nodeResponse;
    }

    private NodeResponse handleCriticalOperation(Command command, ReadableByteChannel channel) throws Exception {
        NodeResponse nodeResponse = NodeResponse.ok();

        if(command == Command.HANDSHAKE) {
            RequestBody requestBody = JsonDataConverter.readNodeRequest(channel, RequestBody.class);
            List<String> values = masterApi.receiveSlaveHandshake(requestBody.getArgs());
            nodeResponse.setData("result", values);
        } else if(command == Command.EXECUTE_REQUESTS) {
            List<NodeRequest> requests = JsonDataConverter.readList(channel, NodeRequest.class);
            for (NodeRequest request : requests) {
                executeRequest(request);
            }
        } else {
            RequestBody requestBody = JsonDataConverter.readNodeRequest(channel, RequestBody.class);
            NodeRequest nodeRequest = new NodeRequest(command, requestBody.getArgs());
            return executeRequest(nodeRequest);
        }

        return nodeResponse;
    }

    private NodeResponse executeRequest(NodeRequest nodeRequest) throws Exception {
        Command command = nodeRequest.getCommand();
        if(command.getType() == CommandType.WRITE) {
            flushContext.put(nodeRequest);
        }

        NodeResponse nodeResponse;
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

        return nodeResponse;
    }

    public void setMasterApi(MasterApi masterApi) {
        this.masterApi = masterApi;
    }

    public void setNextHandler(NodeRequestHandler nextHandler) {
        this.nextHandler = nextHandler;
    }
}
