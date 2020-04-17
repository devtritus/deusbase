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

    public ResponseBody handle(RequestBody requestBody) throws WrongArgumentException {
        Command command = Command.getCommandByName(requestBody.getCommand());
        String[] args = requestBody.getArgs();

        args = CommandParamsUtils.handleParams(command, args);

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
                    return nextHandler.handle(requestBody);
                } else {
                    throw new IllegalArgumentException(String.format("Unhandled command %s", command));
                }
        }

        ResponseBody responseBody = new ResponseBody();
        responseBody.setData(data);
        responseBody.setCode(responseStatus.getCode());

        return responseBody;
    }

    public void setNextHandler(RequestBodyHandler nextHandler){
        this.nextHandler = nextHandler;
    }
}
