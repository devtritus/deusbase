package com.devtritus.deusbase.node.storage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DiskStorageTest {

    private DiskStorage storage;
    private Path path;

    @BeforeEach
    void init() throws IOException {
        Files.createFile(Paths.get("test_storage.bin"));
        storage = new DiskStorage(path);
    }

    @AfterEach
    void after() throws IOException {
        Files.delete(path);
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
