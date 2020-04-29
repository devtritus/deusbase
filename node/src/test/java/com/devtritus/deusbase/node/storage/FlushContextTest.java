package com.devtritus.deusbase.node.storage;

import com.devtritus.deusbase.api.Command;
import com.devtritus.deusbase.api.NodeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.nio.file.Paths;
import static com.devtritus.deusbase.node.utils.Utils.*;
import static org.assertj.core.api.Assertions.assertThat;

class FlushContextTest {
    private final static String TEST_FILE_NAME = "test_flush_context.json";

    private FlushContext flushContext;
    private Path path;

    @BeforeEach
    void init() {
        path = Paths.get(TEST_FILE_NAME);
        deleteFileIfExists(path);
        createFile(path);

        flushContext = new FlushContext(path);
    }

    @Test
    void put_request_test() {
        NodeRequest request = new NodeRequest(Command.CREATE, new String[] { "put_request", "put_request" });
        flushContext.put(request);

        FlushContext anotherFlushContext = new FlushContext(path);

        assertThat(anotherFlushContext.getAll()).containsOnly(request);
    }

    @Test
    void remove_request_test() {
        NodeRequest request = new NodeRequest(Command.CREATE, new String[] { "remove_test", "remove_test" });
        flushContext.put(request);

        assertThat(flushContext.getAll()).containsOnly(request);

        flushContext.remove(request);

        assertThat(flushContext.getAll()).isEmpty();

        FlushContext anotherFlushContext = new FlushContext(path);

        assertThat(anotherFlushContext.getAll()).isEmpty();
    }
}
