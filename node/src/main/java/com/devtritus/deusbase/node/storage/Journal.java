package com.devtritus.deusbase.node.storage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Journal {
    private Path path;
    private int size;
    private long firstBatchStartPosition;
    private long lastBatchStartPosition;

    public Journal(Path path, int size) {
        this.path = path;
        this.size = size;
    }

    public void write(byte[] bytes) {
        try {
            long fileSize = Files.size(path);
            if(fileSize == 0) {
                initHeader();
            } else {
                readHeader();
            }

            try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.WRITE)) {
                channel.position(channel.size());
                long position = channel.size();
                channel.write(ByteBuffer.wrap(bytes));
                long endPosition = channel.position();
                if(endPosition - (lastBatchStartPosition + 8) > size) {
                    ByteBuffer buffer1 = ByteBuffer.allocate(8).putLong(-1);
                    buffer1.rewind();
                    channel.write(buffer1);
                    long endendPosition = channel.position();
                    channel.position(0);

                    buffer1 = ByteBuffer.allocate(256)
                            .putLong(firstBatchStartPosition)
                            .putLong(endPosition);

                    buffer1.rewind();
                    channel.write(buffer1);

                    channel.position(lastBatchStartPosition);

                    buffer1 = ByteBuffer.allocate(8).putLong(endPosition);
                    buffer1.rewind();
                    channel.write(buffer1);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] getFirstBatch() {
        try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.READ)) {
            channel.position(firstBatchStartPosition);
            ByteBuffer buffer = ByteBuffer.allocate(8);
            channel.read(buffer);
            buffer.flip();
            long endOfBuffer = buffer.getLong();
            int batchSize = (int)(endOfBuffer - firstBatchStartPosition - 8);
            byte[] bytes = new byte[batchSize];
            buffer = ByteBuffer.wrap(bytes);
            channel.read(buffer);

            buffer.flip();

            return buffer.array();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void truncate() {
        try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.WRITE, StandardOpenOption.READ)) {
            long size = channel.size();
            if(firstBatchStartPosition > 257) {
                long delta = size - firstBatchStartPosition;
                channel.position(firstBatchStartPosition);
                ByteBuffer buffer = ByteBuffer.allocate(4096);
                long cursor = firstBatchStartPosition;
                long cursor1 = 257;
                int i = 0;
                while((i = channel.read(buffer)) != -1) {
                    cursor += i;
                    buffer.flip();
                    channel.position(cursor1);
                    cursor1 += i;

                    channel.write(buffer);
                }

                channel.truncate(delta);
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void removeFirstBatch() {
        try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.WRITE, StandardOpenOption.READ)) {
            channel.position(firstBatchStartPosition);
            ByteBuffer buffer = ByteBuffer.allocate(8);
            channel.read(buffer);
            buffer.flip();
            long endOfBuffer = buffer.getLong();
            buffer = ByteBuffer.allocate(16).putLong(endOfBuffer).putLong(lastBatchStartPosition);
            buffer.flip();
            channel.write(buffer);
            firstBatchStartPosition = endOfBuffer;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void initHeader() throws IOException {
        firstBatchStartPosition = 256;
        lastBatchStartPosition = 256;
        try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.WRITE)) {
            ByteBuffer buffer = ByteBuffer.allocate(256);
            buffer.putLong(firstBatchStartPosition).putLong(lastBatchStartPosition).rewind();
            channel.write(buffer);
            long position = channel.position();
            ByteBuffer buffer1 = ByteBuffer.allocate(8).putLong(-1);
            buffer1.rewind();
            channel.write(buffer1);
            long position2 = channel.position();
            System.out.println(position2);
        }
    }

    private void readHeader() throws IOException {
        try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(256);
            channel.read(buffer);
            buffer.rewind();

            firstBatchStartPosition = buffer.getLong();
            lastBatchStartPosition = buffer.getLong();
        }
    }

/*
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] valueBytes = key.getBytes(StandardCharsets.UTF_8);

        ByteBuffer buffer = ByteBuffer.allocate(8 + keyBytes.length + valueBytes.length)
                .putInt(keyBytes.length)
                .put(keyBytes)
                .putInt(valueBytes.length)
                .put(valueBytes);


        buffer.flip();
*/
}
