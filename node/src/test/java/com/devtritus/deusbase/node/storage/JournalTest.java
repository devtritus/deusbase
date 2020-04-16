package com.devtritus.deusbase.node.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class JournalTest {
    private final static String TEST_FILE_NAME = "test_journal.bin";
    private final static int BATCH_SIZE = 64;

    private Journal journal;

    String text = "Three days of rest, three blessed days of rest, are what I had with Wolf Larsen, eating at the" +
            "cabin table and doing nothing but discuss life, literature, and the universe, the while Thomas Mugridge" +
            "fumed and raged and did my work as well as his own.";


    @BeforeEach
    void init() throws IOException {
        Path path = Paths.get(TEST_FILE_NAME);
        if(Files.exists(path)) {
            Files.delete(path);
        }

        Files.createFile(path);
        journal = new Journal(path, BATCH_SIZE);
    }

    @Test
    void write_then_get_batch_test() {
        String text1 = text + " dddd";

        journal.write(text.getBytes(StandardCharsets.UTF_8));
        journal.write(text1.getBytes(StandardCharsets.UTF_8));

        byte[] batch = journal.getFirstBatch();
        String actualText = new String(batch, StandardCharsets.UTF_8);
        assertThat(actualText).isEqualTo(text);
    }

    @Test
    void remove_first_batch_test() {
        String text1 = text + " dddd";

        journal.write(text.getBytes(StandardCharsets.UTF_8));
        journal.write(text1.getBytes(StandardCharsets.UTF_8));

        byte[] batch = journal.getFirstBatch();
        String actualText = new String(batch, StandardCharsets.UTF_8);
        assertThat(actualText).isEqualTo(text);

        journal.removeFirstBatch();

        batch = journal.getFirstBatch();
        actualText = new String(batch, StandardCharsets.UTF_8);
        assertThat(actualText).isEqualTo(text1);
    }

    @Test
    void delete_first_batch_test() {
        String text1 = text + " dddd";

        journal.write(text.getBytes(StandardCharsets.UTF_8));
        journal.write(text1.getBytes(StandardCharsets.UTF_8));

        byte[] batch = journal.getFirstBatch();
        String actualText = new String(batch, StandardCharsets.UTF_8);
        assertThat(actualText).isEqualTo(text);

        journal.removeFirstBatch();

        batch = journal.getFirstBatch();
        actualText = new String(batch, StandardCharsets.UTF_8);
        assertThat(actualText).isEqualTo(text1);

        journal.truncate();

        batch = journal.getFirstBatch();
        actualText = new String(batch, StandardCharsets.UTF_8);
        assertThat(actualText).isEqualTo(text1);
    }
}
