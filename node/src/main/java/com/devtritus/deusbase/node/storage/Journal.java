package com.devtritus.deusbase.node.storage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.devtritus.deusbase.node.utils.Utils.isEmptyFile;

class Journal {
    private final static int HEADER_SIZE = 256;
    private final static int LONG_SIZE = 8;
    private final static int COPY_BUFFER_SIZE = 4096;
    private final static int MIN_FILE_SIZE = HEADER_SIZE + LONG_SIZE;

    private final Path path;
    private final int batchSize;

    private int minSizeToTruncate;

    private List<Long> batchPositions;
    private boolean initialized;

    Journal(Path path, int batchSize, int minSizeToTruncate) {
        this.path = path;
        this.batchSize = batchSize;
        this.minSizeToTruncate = minSizeToTruncate;
    }

    void init() {
        try {
            if (isEmptyFile(path)) {
                initJournal();
            } else {
                readJournal();
            }

            initialized = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void write(byte[] bytes) {
        assertJournalInitialized();

        try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.WRITE)) {
            channel.position(channel.size());
            channel.write(ByteBuffer.wrap(bytes));
            long endPosition = channel.position();
            if(endPosition - getLastBatchPosition() > batchSize) {
                writeLong(channel, endPosition, getLastBatchPosition());
                writeLong(channel, -1, endPosition);
                batchPositions.add(endPosition);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    boolean isEmpty() {
        try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.READ)) {
            return isEmpty(channel);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    byte[] getBatch(int position) {
        assertJournalInitialized();

        long batchPosition = batchPositions.get(position);

        try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.READ)) {
            if(isEmpty(channel)) {
                throw new IllegalStateException("Journal is empty");
            }

            long batchEnd = readLong(channel, batchPosition);
            long batchSize;
            if(batchEnd != -1) {
                batchSize = batchEnd - batchPosition - LONG_SIZE;
            } else {
                batchSize = channel.size() - batchPosition - LONG_SIZE;
            }

            ByteBuffer buffer = ByteBuffer.allocate((int)batchSize);
            channel.read(buffer);
            buffer.rewind();
            return buffer.array();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    void removeFirstBatch() {
        assertJournalInitialized();

        try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.WRITE, StandardOpenOption.READ)) {
            long batchEnd = readLong(channel, batchPositions.get(0));
            if(batchEnd != -1) {
                updateHeader(channel, batchEnd);
                truncate(channel);
                batchPositions.remove(0);
            } else if(channel.size() > MIN_FILE_SIZE) {
                channel.truncate(MIN_FILE_SIZE);
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    void truncate() {
        assertJournalInitialized();

        try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.WRITE, StandardOpenOption.READ)) {
            truncate(channel);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    void forceTruncate() {
        assertJournalInitialized();

        try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.WRITE, StandardOpenOption.READ)) {
            forceTruncate(channel);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    void setMinSizeToTruncate(int minSizeToTruncate) {
        this.minSizeToTruncate = minSizeToTruncate;
    }

    int size() {
        if(isEmpty()) {
            return 0;
        }
        return batchPositions.size() - 1;
    }

    private void truncate(SeekableByteChannel channel) throws IOException {
        long delta = batchPositions.get(0) - HEADER_SIZE;
        if(minSizeToTruncate != -1 && delta > minSizeToTruncate) {
            forceTruncate(channel);
        }
    }

    private void forceTruncate(SeekableByteChannel channel) throws IOException {
        assertJournalInitialized();

        long size = channel.size();
        if(batchPositions.get(0) > HEADER_SIZE) {
            long delta = batchPositions.get(0) - HEADER_SIZE;
            long lastPosition = batchPositions.get(0);
            long nextPosition;
            while((nextPosition = readLong(channel, lastPosition)) != -1) {
                long nextNewPosition = nextPosition - delta;
                writeLong(channel, nextNewPosition, lastPosition);
                lastPosition = nextPosition;
            }

            ByteBuffer buffer = ByteBuffer.allocate(COPY_BUFFER_SIZE);
            long cursor1 = HEADER_SIZE;
            long cursor2 = batchPositions.get(0);
            channel.position(cursor2);
            int bytesSize;
            while((bytesSize = channel.read(buffer)) > 0) {
                channel.position(cursor1);
                buffer.flip();
                channel.write(buffer);

                buffer.clear();
                cursor1 += bytesSize;
                cursor2 += bytesSize;
                channel.position(cursor2);
            }

            updateHeader(channel, HEADER_SIZE);

            channel.truncate(size - delta);

            List<Long> oldBatchPositions = new ArrayList<>(batchPositions);
            batchPositions.clear();
            for(Long batchPosition : oldBatchPositions) {
                batchPositions.add(batchPosition - delta);
            }
        }
    }

    private void initJournal() throws IOException {
        batchPositions = new ArrayList<>();

        try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.WRITE)) {
            updateHeader(channel, HEADER_SIZE);
            writeLong(channel, -1, channel.position());
            batchPositions.add((long)HEADER_SIZE);
        }
    }

    private void readJournal() throws IOException {
        batchPositions = new ArrayList<>();

        try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
            channel.read(buffer);
            buffer.rewind();

            long lastPosition = buffer.getLong();
            batchPositions.add(lastPosition);
            long nextPosition;
            while((nextPosition = readLong(channel, lastPosition)) != -1) {
                lastPosition = nextPosition;
                batchPositions.add(nextPosition);
            }
        }
    }

    private void updateHeader(SeekableByteChannel channel, long newFirstBatchStartPosition) throws IOException {
        channel.position(0);

        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE).putLong(newFirstBatchStartPosition);

        buffer.rewind();
        channel.write(buffer);
    }

    private long readLong(SeekableByteChannel channel, long position) throws IOException {
        channel.position(position);
        ByteBuffer buffer = ByteBuffer.allocate(LONG_SIZE);
        channel.read(buffer);
        buffer.rewind();
        return buffer.getLong();
    }

    private void writeLong(SeekableByteChannel channel, long value, long position) throws IOException {
        channel.position(position);
        ByteBuffer buffer = ByteBuffer.allocate(LONG_SIZE).putLong(value);
        buffer.rewind();
        channel.write(buffer);
    }

    private boolean isEmpty(SeekableByteChannel channel) throws IOException {
        long fileSize = channel.size();
        return Objects.equals(getFirstBatchPosition(), getLastBatchPosition()) && fileSize - LONG_SIZE == getLastBatchPosition();
    }

    private Long getFirstBatchPosition() {
        return batchPositions.get(0);
    }

    private Long getLastBatchPosition() {
        return batchPositions.get(batchPositions.size() - 1);
    }

    private void assertJournalInitialized() {
        if(!initialized) {
            throw new RuntimeException("Journal must be initialized");
        }
    }
}
