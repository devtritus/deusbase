package com.devtritus.deusbase.node.storage;

import com.devtritus.deusbase.api.Command;
import com.devtritus.deusbase.api.RequestBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RequestJournalTest {
    private final static String TEST_FILE_NAME = "test_operations_journal.bin";
    private final static int BATCH_SIZE = 256;

    private RequestJournal requestJournal;

    @BeforeEach
    void init() throws IOException {
        Path path = Paths.get(TEST_FILE_NAME);
        if (Files.exists(path)) {
            Files.delete(path);
        }

        Files.createFile(path);
        requestJournal = RequestJournal.create(path, BATCH_SIZE, -1);
    }

    @Test
    void put_get_single_request_test() {
        Command command = Command.CREATE;
        String[] args = new String[] { "Tommy Vercetti", "actor" };
        final RequestBody expected = createResponseBody(command, args);

        assertThat(requestJournal.isEmpty()).isTrue();

        requestJournal.putRequest(command, args);

        List<RequestBody> requests = requestJournal.getRequestsBatch();
        assertThat(requests).containsOnlyOnce(expected);

        assertThat(requestJournal.isEmpty()).isFalse();

        requestJournal.removeFirstRequestsBatch();

        assertThat(requestJournal.isEmpty()).isTrue();
    }

    @Test
    void put_get_two_requests_test() {
        final Command command1 = Command.CREATE;
        final String[] args1 = new String[] { "Albert Einstein", "theoretical physicist who developed the theory of relativity, one of the two pillars of modern physics" };
        final RequestBody expected1 = createResponseBody(command1, args1);

        final Command command2 = Command.UPDATE;
        final String[] args2 = new String[] { "Waldemar Haffkine", "bacteriologist from Ukraine. He emigrated and worked at the Pasteur Institute in Paris, where he developed an anti-cholera vaccine that he tried out successfully in India." };
        final RequestBody expected2 = createResponseBody(command2, args2);

        final Command command3 = Command.DELETE;
        final String[] args3 = new String[] { "Andrey Kolmogorov", "mathematician who made significant contributions to the mathematics of probability theory, topology, intuitionistic logic, turbulence, classical mechanics, algorithmic information theory and computational complexity." };
        final RequestBody expected3 = createResponseBody(command3, args3);

        assertThat(requestJournal.isEmpty()).isTrue();

        requestJournal.putRequest(command1, args1);

        List<RequestBody> requests1 = requestJournal.getRequestsBatch();
        assertThat(requests1).containsOnly(expected1);

        requestJournal.putRequest(command2, args2);

        List<RequestBody> requests2 = requestJournal.getRequestsBatch();
        assertThat(requests2).containsOnly(expected1, expected2);

        requestJournal.putRequest(command3, args3);

        List<RequestBody> requests3 = requestJournal.getRequestsBatch();
        assertThat(requests3).containsOnly(expected1, expected2);

        assertThat(requestJournal.isEmpty()).isFalse();

        requestJournal.removeFirstRequestsBatch();

        List<RequestBody> requests4 = requestJournal.getRequestsBatch();
        assertThat(requests4).containsOnly(expected3);

        requestJournal.removeFirstRequestsBatch();

        assertThat(requestJournal.isEmpty()).isTrue();
    }

    private RequestBody createResponseBody(Command command, String... args) {
        RequestBody requestBody = new RequestBody();
        requestBody.setCommand(command.toString());
        requestBody.setArgs(args);
        return requestBody;
    }
}
