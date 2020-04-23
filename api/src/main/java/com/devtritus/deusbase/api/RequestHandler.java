package com.devtritus.deusbase.api;

import java.nio.channels.ReadableByteChannel;

public interface RequestHandler {
    byte[] handle(Command command, ReadableByteChannel data) throws WrongArgumentException;
}
