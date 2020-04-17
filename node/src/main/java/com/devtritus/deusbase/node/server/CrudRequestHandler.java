package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.api.*;

import java.util.List;
import java.util.Map;

public class CrudRequestHandler implements RequestBodyHandler {
    private Api<String, String> api;
    private RequestBodyHandler nextHandler;

    public CrudRequestHandler(Api<String, String> api) {
        this.api = api;
    }

    public NodeResponse handle(NodeRequest request) throws WrongArgumentException {
        final Command command = request.getCommand();

        String[] args = CommandParamsUtils.handleParams(command, request.getArgs());

        Map<String, List<String>> data;
        ResponseStatus responseStatus = ResponseStatus.OK;

        switch (command) {
            case READ:
                data = api.read(args[0]);
                if(data.isEmpty()){
                    responseStatus = ResponseStatus.NOT_FOUND;
                }
                break;
            case SEARCH:
                data = api.search(args[0]);
                if(data.isEmpty()){
                    responseStatus = ResponseStatus.NOT_FOUND;
                }
                break;
            case CREATE:
                data = api.create(args[0], args[1]);
                break;
            case DELETE:
                data = api.delete(args[0], Integer.parseInt(args[1]));
                break;
            case UPDATE:
                data = api.update(args[0], Integer.parseInt(args[1]), args[2]);
                break;
            default:
                if(nextHandler != null) {
                    return nextHandler.handle(request);
                } else {
                    throw new IllegalArgumentException(String.format("Unhandled command %s", command));
                }
        }

        NodeResponse response = new NodeResponse();
        response.setData(data);
        response.setCode(responseStatus.getCode());

        return response;
    }

    public void setNextHandler(RequestBodyHandler nextHandler){
        this.nextHandler = nextHandler;
    }
}
