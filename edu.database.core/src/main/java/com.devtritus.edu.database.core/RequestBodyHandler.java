package com.devtritus.edu.database.core;

import java.util.Map;

public class RequestBodyHandler {
    private Api<String, String> api;

    public RequestBodyHandler(Api<String, String> api) {
        this.api = api;
    }

    public ResponseBody handle(RequestBody requestBody) throws WrongArgumentsCountException {
        Command command = Command.getCommand(requestBody.getCommand());
        String[] args = requestBody.getArgs();

        CommandParamsValidator.validate(command, args);

        Map<String, String> data;
        ResponseStatus responseStatus = ResponseStatus.OK;

        switch (command) {
            case CREATE:
                data = api.create(args[0], args[1]);
                break;
            case READ:
                data = api.read(args[0]);
                if(data.get(args[0]) == null) {
                    responseStatus = ResponseStatus.NOT_FOUND;
                }
                break;
            case DELETE:
                data = api.delete(args[0]);
                break;
            case UPDATE:
                data = api.update(args[0], args[1]);
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
