package com.devtritus.deusbase.node.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import static com.devtritus.deusbase.node.TestUtils.createTempFile;
import static org.assertj.core.api.Assertions.assertThat;

class ValueStorageTest {
    private final static String TEST_FILE_NAME = "test_storage.bin";

    private ValueStorage storage;

    @BeforeEach
    void init() {
        Path path = createTempFile(TEST_FILE_NAME);

        storage = new ValueStorage(path);
    }

    @Test
    void write_test() {
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
