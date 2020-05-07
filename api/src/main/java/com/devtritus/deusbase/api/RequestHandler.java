package com.devtritus.deusbase.api;

import java.nio.channels.ReadableByteChannel;

public interface RequestHandler {
    NodeResponse handle(Command command, ReadableByteChannel channel);
}
