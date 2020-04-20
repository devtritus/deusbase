package com.devtritus.deusbase.node.storage;

import com.devtritus.deusbase.api.Command;
import com.devtritus.deusbase.api.NodeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.devtritus.deusbase.node.utils.Utils.*;
import static org.assertj.core.api.Assertions.assertThat;

class RequestJournalTest {
    private final static String TEST_FILE_NAME = "test_operations_journal.bin";
    private final static int BATCH_SIZE = 256;

    private RequestJournal requestJournal;

    @BeforeEach
    void init() {
        Path path = Paths.get(TEST_FILE_NAME);
        deleteFileIfExists(path);
        createFile(path);

        requestJournal = RequestJournal.init(path, null, BATCH_SIZE, -1);
    }

    @Test
    void put_get_single_request_test() {
        final NodeRequest expected = createRequest(Command.CREATE,  "Tommy Vercetti", "actor" );

        assertThat(requestJournal.isEmpty()).isTrue();

        requestJournal.putRequest(expected);

        List<NodeRequest> requests = requestJournal.getRequestsBatch(0);
        assertThat(requests).containsOnlyOnce(expected);

        assertThat(requestJournal.isEmpty()).isFalse();

        requestJournal.removeFirstRequestsBatch();

        assertThat(requestJournal.isEmpty()).isTrue();
    }

    @Test
    void put_get_two_requests_test() {
        final NodeRequest expected1 = createRequest(
                Command.CREATE,
                "Albert Einstein",
                "theoretical physicist who developed the theory of relativity, one of the two pillars of modern physics" );

        final NodeRequest expected2 = createRequest(
                Command.UPDATE,
                "Waldemar Haffkine",
                "bacteriologist from Ukraine. He emigrated and worked at the Pasteur Institute in Paris, where he developed an anti-cholera vaccine that he tried out successfully in India." );

        final NodeRequest expected3 = createRequest(
                Command.DELETE,
                "Andrey Kolmogorov",
                "mathematician who made significant contributions to the mathematics of probability theory, topology, intuitionistic logic, turbulence, classical mechanics, algorithmic information theory and computational complexity." );

        assertThat(requestJournal.isEmpty()).isTrue();

        requestJournal.putRequest(expected1);

        List<NodeRequest> requests1 = requestJournal.getRequestsBatch(0);
        assertThat(requests1).containsOnly(expected1);

        requestJournal.putRequest(expected2);

        List<NodeRequest> requests2 = requestJournal.getRequestsBatch(0);
        assertThat(requests2).containsOnly(expected1, expected2);

        requestJournal.putRequest(expected3);

        List<NodeRequest> requests3 = requestJournal.getRequestsBatch(0);
        assertThat(requests3).containsOnly(expected1, expected2);

        assertThat(requestJournal.isEmpty()).isFalse();

        requestJournal.removeFirstRequestsBatch();

        List<NodeRequest> requests4 = requestJournal.getRequestsBatch(0);
        assertThat(requests4).containsOnly(expected3);

        requestJournal.removeFirstRequestsBatch();

        assertThat(requestJournal.isEmpty()).isTrue();
    }

    private NodeRequest createRequest(Command command, String... args) {
        return new NodeRequest(command, args);
    }
}
