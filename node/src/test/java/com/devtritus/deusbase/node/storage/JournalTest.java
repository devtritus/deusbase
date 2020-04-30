package com.devtritus.deusbase.node.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    private final String test_text_2 = "Coming now to the other qualities mentioned above, I say that every prince ought" +
            "to desire to be considered clement and not cruel. Nevertheless he ought to take care not to misuse this clemency.";

    private final String test_text_3 = "“Have you a couple of days to spare? Have just been wired for from the west of England" +
            "in connection with Boscombe Valley tragedy. Shall be glad if you will come with me. Air and scenery perfect. Leave Paddington by the 11.15.”";

    private Journal journal;

    @BeforeEach
    void init() {
        Path path = Paths.get(TEST_FILE_NAME);
        deleteFileIfExists(path);
        createFile(path);
        journal = new Journal(path, BATCH_SIZE, -1);
        journal.init();
    }

    @Test
    void remove_batches_test() {
        journal.write(utf8StringToBytes("test"));

        journal.removeBatches(1);

        assertThat(journal.size()).isEqualTo(0);

        journal.write(utf8StringToBytes(test_text_1));
        journal.write(utf8StringToBytes(test_text_2));

        assertThat(journal.size()).isEqualTo(2);
        journal.removeBatches(2);

        assertThat(journal.size()).isEqualTo(0);
        assertThat(journal.isLastBatchEmpty()).isTrue();

        journal.write(utf8StringToBytes(test_text_1));
        journal.write(utf8StringToBytes(test_text_2));
        journal.write(utf8StringToBytes(test_text_3));

        assertThat(journal.size()).isEqualTo(3);

        journal.removeBatches(2);

        assertThat(journal.size()).isEqualTo(1);

        journal.removeBatches(1);

        assertThat(journal.size()).isEqualTo(0);
        assertThat(journal.isLastBatchEmpty()).isTrue();
    }

    @Test
    void is_last_batch_empty_test() {
        assertThat(journal.isLastBatchEmpty()).isTrue();

        journal.write(utf8StringToBytes("test"));

        assertThat(journal.isLastBatchEmpty()).isFalse();
        assertThat(journal.size()).isEqualTo(1);

        journal.write(utf8StringToBytes(test_text_1)); //first batch will be full so second batch is empty

        assertThat(journal.size()).isEqualTo(1);
        assertThat(journal.isLastBatchEmpty()).isTrue();

        journal.removeFirstBatch();

        assertThat(journal.isLastBatchEmpty()).isTrue();
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

        assertFirstBatchContains(shortString);

        journal.removeFirstBatch();

        assertThatThrownBy(() -> journal.getBatch(0)).isInstanceOf(RuntimeException.class);
        assertThat(journal.isEmpty()).isTrue();
    }

    @Test
    void get_batch_when_journal_is_empty_test() {
        assertThatThrownBy(() -> journal.getBatch(0)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void get_batch_when_journal_contains_single_batch() {
        final String shortString = "test_string";
        journal.write(utf8StringToBytes(shortString));

        assertFirstBatchContains(shortString);
    }

    @Test
    void write_then_get_batch_test() {
        journal.write(utf8StringToBytes(test_text_1));
        journal.write(utf8StringToBytes(test_text_2));

        assertFirstBatchContains(test_text_1);
    }

    @Test
    void get_size_test() {
        assertThat(journal.size()).isEqualTo(0);

        journal.write(utf8StringToBytes(test_text_1));

        assertThat(journal.size()).isEqualTo(1);

        journal.write(utf8StringToBytes(test_text_2));

        assertThat(journal.size()).isEqualTo(2);

        journal.removeFirstBatch();

        assertThat(journal.size()).isEqualTo(1);

        journal.removeFirstBatch();

        assertThat(journal.size()).isEqualTo(0);
    }

    @Test
    void remove_first_batch_test() {
        journal.write(utf8StringToBytes(test_text_1));
        journal.write(utf8StringToBytes(test_text_2));

        assertFirstBatchContains(test_text_1);

        journal.removeFirstBatch();

        assertFirstBatchContains(test_text_2);
    }

    @Test
    void delete_first_batch_test() {
        journal.write(utf8StringToBytes(test_text_1));
        journal.write(utf8StringToBytes(test_text_2));

        assertFirstBatchContains(test_text_1);

        journal.removeFirstBatch();

        assertFirstBatchContains(test_text_2);

        journal.forceTruncate();

        assertFirstBatchContains(test_text_2);
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
    void get_batch_test() {
        assertThat(journal.isEmpty()).isTrue();

        journal.write(utf8StringToBytes(test_text_1));

        assertBatchContains(0, test_text_1);

        journal.write(utf8StringToBytes(test_text_2));

        assertBatchContains(0, test_text_1);
        assertBatchContains(1, test_text_2);

        journal.write(utf8StringToBytes(test_text_3));

        assertBatchContains(0, test_text_1);
        assertBatchContains(1, test_text_2);
        assertBatchContains(2, test_text_3);

        journal.removeFirstBatch();

        assertBatchContains(0, test_text_2);
        assertBatchContains(1, test_text_3);

        journal.removeFirstBatch();

        assertBatchContains(0, test_text_3);

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

            byte[] batch = journal.getBatch(0);
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
            byte[] batch = journal.getBatch(0);
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
            byte[] batch = journal.getBatch(0);
            String actualText = bytesToUtf8String(batch);
            assertThat(strings).contains(actualText);
            strings.remove(actualText);
            journal.removeFirstBatch();
            journal.forceTruncate();
        }

        assertThat(strings).isEmpty();
    }

    private void assertFirstBatchContains(String expectedText) {
        assertBatchContains(0, expectedText);
    }

    private void assertBatchContains(int position, String expectedText) {
        byte[] batch = journal.getBatch(position);
        String actualText = bytesToUtf8String(batch);
        assertThat(actualText).isEqualTo(expectedText);
    }
}
