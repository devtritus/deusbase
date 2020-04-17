package com.devtritus.deusbase.api;

public interface RequestBodyHandler {
    NodeResponse handle(NodeRequest request) throws WrongArgumentException;

    void setNextHandler(RequestBodyHandler nextHandler);
}
