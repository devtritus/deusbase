package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.api.*;

import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.Map;

public class CrudRequestHandler implements RequestBodyHandler {
    private Api<String, String> api;
    private RequestBodyHandler nextHandler;

    public CrudRequestHandler(Api<String, String> api) {
        this.api = api;
    }

    @Override
    public byte[] handle(Command command, ReadableByteChannel channel) throws WrongArgumentException {
        Map<String, List<String>> data;
        ResponseStatus responseStatus = ResponseStatus.OK;
        String[] args = null;
        switch (command) {
            case READ:
                args = getArgs(command, channel);
                data = api.read(args[0]);
                if(data.isEmpty()){
                    responseStatus = ResponseStatus.NOT_FOUND;
                }
                break;
            case SEARCH:
                args = getArgs(command, channel);
                data = api.search(args[0]);
                if(data.isEmpty()){
                    responseStatus = ResponseStatus.NOT_FOUND;
                }
                break;
            case CREATE:
                args = getArgs(command, channel);
                data = api.create(args[0], args[1]);
                break;
            case DELETE:
                args = getArgs(command, channel);
                data = api.delete(args[0], Integer.parseInt(args[1]));
                break;
            case UPDATE:
                args = getArgs(command, channel);
                data = api.update(args[0], Integer.parseInt(args[1]), args[2]);
                break;
            default:
                if(nextHandler != null) {
                    return nextHandler.handle(command, channel);
                } else {
                    throw new IllegalArgumentException(String.format("Unhandled command %s", command));
                }
        }

        NodeResponse response = new NodeResponse();
        response.setData(data);
        response.setCode(responseStatus.getCode());

        return JsonDataConverter.convertObjectToJsonBytes(response);
    }

    public void setNextHandler(RequestBodyHandler nextHandler){
        this.nextHandler = nextHandler;
    }

    private String[] getArgs(Command command, ReadableByteChannel channel) throws WrongArgumentException {
        RequestBody requestBody = JsonDataConverter.readStringFromChannel(channel, RequestBody.class);
        return CommandParamsUtils.handleParams(command, requestBody.getArgs());
    }
}
