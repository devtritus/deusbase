package com.devtritus.deusbase.node.storage;

import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ByteStorage {
    private final Path path;

    ByteStorage(Path path) {
        this.path = path;
    }

    long write(byte[] bytes) {
        try(SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.APPEND)) {
            int valueSize = bytes.length;
            ByteBuffer buffer = ByteBuffer.allocate(8 + valueSize)
                    .putInt(valueSize)
                    .put(bytes);

            buffer.rewind();

            long startPosition = channel.position();

            channel.write(buffer);
            return startPosition;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    Map<Long, byte[]> read(List<Long> addresses) {
        try(SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.READ)) {
            Map<Long, byte[]> result = new HashMap<>();
            for(Long address : addresses) {
                channel.position(address);
                ByteBuffer buffer = ByteBuffer.allocate(4);
                channel.read(buffer);
                buffer.flip();
                int valueSize = buffer.getInt();
                ByteBuffer valueBuffer = ByteBuffer.allocate(valueSize);
                channel.read(valueBuffer);
                result.put(address, valueBuffer.array());
            }

            return result;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
