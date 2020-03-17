package com.devtritus.edu.database.node.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ValueDiskStorageTest {

    private ValueDiskStorage storage;

    @BeforeEach
    void init() throws IOException {
        Path path = Paths.get("test.storage");
        if(Files.exists(path)) {
            Files.delete(path);
        }

        storage = new ValueDiskStorage(path);
    }

    @Test
    void test() {
        long position = storage.write("aaa");
        assertThatStorageContains(position, "aaa");

        long position1 = storage.write("bbbb");
        assertThatStorageContains(position1, "bbbb");

        long position2 = storage.write("ccccc");
        assertThatStorageContains(position2, "ccccc");

        assertThatStorageContains(position1, "bbbb");

        assertThatStorageContains(position, "aaa");
    }

    private void assertThatStorageContains(long position, String value) {
        Map<Long, String> result = storage.read(Collections.singletonList(position));
        assertThat(result.get(position)).isEqualTo(value);
    }
}
