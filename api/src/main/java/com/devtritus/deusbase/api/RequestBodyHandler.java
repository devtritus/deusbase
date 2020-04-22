package com.devtritus.deusbase.api;

import java.nio.channels.ReadableByteChannel;

public interface RequestBodyHandler {
    byte[] handle(Command command, ReadableByteChannel data) throws WrongArgumentException;

    void setNextHandler(RequestBodyHandler nextHandler);
}
