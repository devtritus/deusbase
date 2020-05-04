package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.api.*;
import com.devtritus.deusbase.node.env.NodeEnvironment;
import com.devtritus.deusbase.node.role.SlaveApi;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class SlaveRequestHandler implements RequestHandler {
    private final NodeEnvironment env;

    private SlaveApi slaveApi;
    private NodeRequestHandler nextHandler;

    public SlaveRequestHandler(NodeEnvironment env) {
        this.env = env;
    }

    @Override
    public byte[] handle(Command command, ReadableByteChannel channel) throws WrongArgumentException {
        NodeResponse nodeResponse = new NodeResponse();
        if(command == Command.SYNC_COMPLETE) {
            slaveApi.handleSyncComplete();
        } else if(command == Command.BATCH) {
            List<NodeRequest> requests = slaveApi.receiveLogBatch(channel);

            for (NodeRequest request1 : requests) {
                nextHandler.handle(request1);
            }
        } else if(command == Command.COPY_INDEX) {
            copyDataToFile(channel, env.getIndexPath());
        } else if(command == Command.COPY_STORAGE) {
            copyDataToFile(channel, env.getStoragePath());
        } else if(command == Command.WRITE_PROPERTY) {
            RequestBody requestBody = JsonDataConverter.readNodeRequest(channel, RequestBody.class);
            String[] args = requestBody.getArgs();
            env.putProperty(args[0], args[1]);
        } else if(command.getType() == CommandType.WRITE) {
            throwSlaveException();
        } else {
            RequestBody requestBody = JsonDataConverter.readNodeRequest(channel, RequestBody.class);
            NodeRequest nodeRequest = new NodeRequest(command, requestBody.getArgs());
            nodeResponse = nextHandler.handle(nodeRequest);
        }

        return JsonDataConverter.convertObjectToJsonBytes(nodeResponse);
    }

    public void setNextHandler(NodeRequestHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    public void setSlaveApi(SlaveApi slaveApi) {
        this.slaveApi = slaveApi;
    }

    private void throwSlaveException() {
        throw new IllegalStateException("Node has been running as SLAVE. Write operations are forbidden");
    }

    private void copyDataToFile(ReadableByteChannel channel, Path path) {
        try(WritableByteChannel writer = Files.newByteChannel(path, StandardOpenOption.WRITE)) {
            ByteBuffer buffer = ByteBuffer.allocate(4096);

            while(channel.read(buffer) != -1) {
                buffer.flip();
                writer.write(buffer);
                buffer.rewind();
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
