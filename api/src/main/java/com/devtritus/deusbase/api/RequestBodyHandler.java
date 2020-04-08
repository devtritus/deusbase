package com.devtritus.deusbase.api;

public interface RequestBodyHandler {
    ResponseBody handle(RequestBody requestBody) throws WrongArgumentException;

    void setNextHandler(RequestBodyHandler nextHandler);
}
