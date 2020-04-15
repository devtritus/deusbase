package com.devtritus.deusbase.node.storage;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StringStorage {
    private final ByteStorage storage;

    public StringStorage(Path path) {
        this.storage = new ByteStorage(path);
    }

    public long write(String value) {
        byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
        return storage.write(valueBytes);
    }

    public Map<Long, String> read(List<Long> addresses) {
        Map<Long, byte[]> result = storage.read(addresses);
        return result.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new String(entry.getValue(), StandardCharsets.UTF_8)));
    }
}
