package com.devtritus.edu.database.node.storage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValueDiskStorage implements ValueStorage {
    private final Path path;

    public ValueDiskStorage(Path path) {
        this.path = path;

        try {
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long put(String value) {
        try(SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.APPEND)) {
            byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
            int valueSize = valueBytes.length;
            ByteBuffer buffer = ByteBuffer.allocate(8 + valueSize)
                    .putInt(valueSize)
                    .put(valueBytes);

            buffer.rewind();

            long startPosition = channel.position();

            channel.write(buffer);
            return startPosition;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<Long, String> get(List<Long> addresses) {
        try(SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.READ)) {
            Map<Long, String> result = new HashMap<>();
            for(Long address : addresses) {
                channel.position(address);
                ByteBuffer buffer = ByteBuffer.allocate(4);
                channel.read(buffer);
                buffer.flip();
                int valueSize = buffer.getInt();
                ByteBuffer valueBuffer = ByteBuffer.allocate(valueSize);
                channel.read(valueBuffer);
                String value = new String(valueBuffer.array(), StandardCharsets.UTF_8);
                result.put(address, value);
            }

            return result;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
