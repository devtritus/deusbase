package com.devtritus.edu.database.core;

import java.util.List;
import java.util.Map;

public class RequestBodyHandler {
    private Api<String, String> api;

    public RequestBodyHandler(Api<String, String> api) {
        this.api = api;
    }

    public ResponseBody handle(RequestBody requestBody) throws WrongArgumentsCountException {
        Command command = Command.getCommand(requestBody.getCommand());
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
                throw new IllegalArgumentException("Unknown command: " + command);
        }

        ResponseBody responseBody = new ResponseBody();
        responseBody.setData(data);
        responseBody.setCode(responseStatus.getCode());

        return responseBody;
    }
}
