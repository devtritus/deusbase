package com.devtritus.deusbase.node.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.devtritus.deusbase.node.TestUtils.*;
import static com.devtritus.deusbase.node.utils.Utils.*;
import static org.assertj.core.api.Assertions.*;

class JournalTest {
    private final static String TEST_FILE_NAME = "test_journal.bin";
    private final static int BATCH_SIZE = 64;

    private final String test_text_1 = "Three days of rest, three blessed days of rest, are what I had with Wolf Larsen, eating at the" +
            "cabin table and doing nothing but discuss life, literature, and the universe, the while Thomas Mugridge" +
            "fumed and raged and did my work as well as his own.";

    private final String test_text_2 = new StringBuilder(test_text_1).reverse().toString();

    private Journal journal;

    @BeforeEach
    void init() throws IOException {
        Path path = Paths.get(TEST_FILE_NAME);
        if (Files.exists(path)) {
            Files.delete(path);
        }

        Files.createFile(path);
        journal = new Journal(path, BATCH_SIZE, -1);
        journal.init();
    }

    @Test
    void remove_batch_when_journal_is_empty_test() {
        assertThat(journal.isEmpty()).isTrue();

        assertThatCode(() -> journal.removeFirstBatch()).doesNotThrowAnyException();

        assertThat(journal.isEmpty()).isTrue();
    }

    @Test
    void truncate_batch_when_journal_is_empty_test() {
        assertThat(journal.isEmpty()).isTrue();

        assertThatCode(() -> journal.forceTruncate()).doesNotThrowAnyException();

        assertThat(journal.isEmpty()).isTrue();
    }

    @Test
    void remove_single_first_batch_test() {
        final String shortString = "test_string";

        journal.write(utf8StringToBytes(shortString));
        byte[] batch = journal.getFirstBatch();

        assertThat(bytesToUtf8String(batch)).isEqualTo(shortString);

        journal.removeFirstBatch();

        assertThatThrownBy(() -> journal.getFirstBatch()).isInstanceOf(RuntimeException.class);
        assertThat(journal.isEmpty()).isTrue();
    }

    @Test
    void get_batch_when_journal_is_empty_test() {
        assertThatThrownBy(() -> journal.getFirstBatch()).isInstanceOf(RuntimeException.class);
    }

    @Test
    void get_batch_when_journal_contains_single_batch() {
        final String shortString = "test_string";
        journal.write(utf8StringToBytes(shortString));

        byte[] batch = journal.getFirstBatch();
        String actualString = bytesToUtf8String(batch);
        assertThat(actualString).isEqualTo(shortString);
    }

    @Test
    void write_then_get_batch_test() {
        journal.write(utf8StringToBytes(test_text_1));
        journal.write(utf8StringToBytes(test_text_2));

        byte[] batch = journal.getFirstBatch();
        String actualText = bytesToUtf8String(batch);
        assertThat(actualText).isEqualTo(test_text_1);
    }

    @Test
    void remove_first_batch_test() {
        journal.write(utf8StringToBytes(test_text_1));
        journal.write(utf8StringToBytes(test_text_2));

        byte[] batch = journal.getFirstBatch();
        String actualText = bytesToUtf8String(batch);
        assertThat(actualText).isEqualTo(test_text_1);

        journal.removeFirstBatch();

        batch = journal.getFirstBatch();
        actualText = bytesToUtf8String(batch);
        assertThat(actualText).isEqualTo(test_text_2);
    }

    @Test
    void delete_first_batch_test() {
        journal.write(utf8StringToBytes(test_text_1));
        journal.write(utf8StringToBytes(test_text_2));

        byte[] batch = journal.getFirstBatch();
        String actualText = bytesToUtf8String(batch);
        assertThat(actualText).isEqualTo(test_text_1);

        journal.removeFirstBatch();

        batch = journal.getFirstBatch();
        actualText = bytesToUtf8String(batch);
        assertThat(actualText).isEqualTo(test_text_2);

        journal.forceTruncate();

        batch = journal.getFirstBatch();
        actualText = bytesToUtf8String(batch);
        assertThat(actualText).isEqualTo(test_text_2);
    }

    @Test
    void complex_is_empty_test() {
        assertThat(journal.isEmpty()).isTrue();

        journal.write(utf8StringToBytes(test_text_1));

        assertThat(journal.isEmpty()).isFalse();

        journal.removeFirstBatch();
        journal.forceTruncate();

        assertThat(journal.isEmpty()).isTrue();

        journal.write(utf8StringToBytes(test_text_1));
        journal.removeFirstBatch();

        assertThat(journal.isEmpty()).isTrue();
    }

    @Test
    void complex_test_without_truncate() {
        //BATCH_SIZE must be less than size of each string
        List<String> strings = getRandomStrings(100, 150, 1000);

        for (String string : getShuffledList(strings)) {
            journal.write(utf8StringToBytes(string));
        }

        while (!journal.isEmpty()) {
            byte[] batch = journal.getFirstBatch();
            String actualText = bytesToUtf8String(batch);
            assertThat(strings).contains(actualText);
            strings.remove(actualText);
            journal.removeFirstBatch();
        }

        assertThat(strings).isEmpty();
    }

    @Test
    void complex_test_with_regular_truncate() {
        List<String> strings = getRandomStrings(100, 150, 1000);
        journal.setMinSizeToTruncate(1024);

        for (String string : getShuffledList(strings)) {
            journal.write(utf8StringToBytes(string));
        }

        while (!journal.isEmpty()) {
            byte[] batch = journal.getFirstBatch();
            String actualText = bytesToUtf8String(batch);
            assertThat(strings).contains(actualText);
            strings.remove(actualText);
            journal.removeFirstBatch();
        }

        assertThat(strings).isEmpty();
    }

    @Test
    void complex_test_with_truncate() {
        List<String> strings = getRandomStrings(100, 150, 1000);

        for (String string : getShuffledList(strings)) {
            journal.write(utf8StringToBytes(string));
        }

        while (!journal.isEmpty()) {
            byte[] batch = journal.getFirstBatch();
            String actualText = bytesToUtf8String(batch);
            assertThat(strings).contains(actualText);
            strings.remove(actualText);
            journal.removeFirstBatch();
            journal.forceTruncate();
        }

        assertThat(strings).isEmpty();
    }
}
