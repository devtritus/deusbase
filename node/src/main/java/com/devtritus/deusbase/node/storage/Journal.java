package com.devtritus.deusbase.node.storage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

class Journal {
    private final static int HEADER_SIZE = 256;
    private final static int LONG_SIZE = 8;
    private final static int COPY_BUFFER_SIZE = 4096;
    private final static int MIN_FILE_SIZE = HEADER_SIZE + LONG_SIZE;

    private Path path;
    private int batchSize;
    private int minSizeToTruncate;

    private boolean initialized;
    private long firstBatchStartPosition;
    private long lastBatchStartPosition;

    Journal(Path path, int batchSize, int minSizeToTruncate) {
        this.path = path;
        this.batchSize = batchSize;
        this.minSizeToTruncate = minSizeToTruncate;
    }

    void init() {
        try {
            long fileSize = Files.size(path);
            if (fileSize == 0) {
                initHeader();
            } else {
                readHeader();
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
            if(endPosition - lastBatchStartPosition > batchSize) {
                writeLong(channel, endPosition, lastBatchStartPosition);
                updateHeader(channel, firstBatchStartPosition, endPosition);
                writeLong(channel, -1, endPosition);
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

    byte[] getFirstBatch() {
        assertJournalInitialized();

        try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.READ)) {
            if(isEmpty(channel)) {
                throw new IllegalStateException("Journal is empty");
            }

            long batchEnd = readLong(channel, firstBatchStartPosition);
            long batchSize;
            if(batchEnd != -1) {
                batchSize = batchEnd - firstBatchStartPosition - LONG_SIZE;
            } else {
                batchSize = channel.size() - firstBatchStartPosition - LONG_SIZE;
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
            long batchEnd = readLong(channel, firstBatchStartPosition);
            if(batchEnd != -1) {
                updateHeader(channel, batchEnd, lastBatchStartPosition);

                truncate(channel);
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

    private void truncate(SeekableByteChannel channel) throws IOException {
        long delta = firstBatchStartPosition - HEADER_SIZE;
        if(minSizeToTruncate != -1 && delta > minSizeToTruncate) {
            forceTruncate(channel);
        }
    }

    private void forceTruncate(SeekableByteChannel channel) throws IOException {
        assertJournalInitialized();

        long size = channel.size();
        if(firstBatchStartPosition > HEADER_SIZE) {
            long delta = firstBatchStartPosition - HEADER_SIZE;
            long lastPosition = firstBatchStartPosition;
            long nextPosition;
            while((nextPosition = readLong(channel, lastPosition)) != -1) {
                long newPosition = nextPosition - delta;
                writeLong(channel, newPosition, lastPosition);
                lastPosition = nextPosition;
            }

            ByteBuffer buffer = ByteBuffer.allocate(COPY_BUFFER_SIZE);
            long cursor1 = HEADER_SIZE;
            long cursor2 = firstBatchStartPosition;
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

            updateHeader(channel, HEADER_SIZE, lastBatchStartPosition - delta);

            channel.truncate(size - delta);
        }
    }

    private void initHeader() throws IOException {
        try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.WRITE)) {
            updateHeader(channel, HEADER_SIZE, HEADER_SIZE);
            writeLong(channel, -1, channel.position());
        }
    }

    private void readHeader() throws IOException {
        try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
            channel.read(buffer);
            buffer.rewind();

            firstBatchStartPosition = buffer.getLong();
            lastBatchStartPosition = buffer.getLong();
        }
    }

    private void updateHeader(SeekableByteChannel channel, long newFirstBatchStartPosition, long newLastBatchStartPosition) throws IOException {
        channel.position(0);

        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE)
                .putLong(newFirstBatchStartPosition)
                .putLong(newLastBatchStartPosition);

        buffer.rewind();
        channel.write(buffer);

        firstBatchStartPosition = newFirstBatchStartPosition;
        lastBatchStartPosition = newLastBatchStartPosition;
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
        return firstBatchStartPosition == lastBatchStartPosition && fileSize - LONG_SIZE == lastBatchStartPosition;
    }

    private void assertJournalInitialized() {
        if(!initialized) {
            throw new RuntimeException("Journal must be initialized");
        }
    }
}
