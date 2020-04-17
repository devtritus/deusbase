package com.devtritus.deusbase.node.storage;

import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.devtritus.deusbase.node.utils.Utils.bytesToUtf8String;
import static com.devtritus.deusbase.node.utils.Utils.utf8StringToBytes;

public class ValueStorage {
    private final Path path;

    public ValueStorage(Path path) {
        this.path = path;
    }

    public long write(String value) {
        byte[] bytes = utf8StringToBytes(value);
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

    public Map<Long, String> read(List<Long> addresses) {
        Map<Long, byte[]> result = new HashMap<>();
        try(SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.READ)) {
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
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        return result.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> bytesToUtf8String(entry.getValue())));
    }
}
