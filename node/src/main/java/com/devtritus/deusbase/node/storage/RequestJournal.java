package com.devtritus.deusbase.node.storage;

import com.devtritus.deusbase.api.Command;
import com.devtritus.deusbase.api.NodeRequest;

import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static com.devtritus.deusbase.node.utils.Utils.*;

public class RequestJournal {
    private final static int INTEGER_SIZE = 4;

    private final Journal journal;
    private final FlushContext flushContext;
    private final Path batchIdPath;
    private long id;

    private RequestJournal(Journal journal, FlushContext flushContext, Path batchIdPath, long id) {
        this.journal = journal;
        this.flushContext = flushContext;
        this.batchIdPath = batchIdPath;
        this.id = id;
    }

    public long getFirstBatchId() {
        return id;
    }

    public static RequestJournal init(Path journalPath, FlushContext flushContext, int batchSize, int minSizeToTruncate) {
        Path batchIdPath = Paths.get("batch.bin");
        //TODO: store ids in the journal
        createFileIfNotExist(batchIdPath);
        Journal journal = new Journal(journalPath, batchSize, minSizeToTruncate);
        journal.init();

        long id = 0;
        try {
            if (Files.size(batchIdPath) == 0) {
                ByteBuffer buffer = ByteBuffer.allocate(8);
                buffer.putLong(0);
                buffer.flip();
                try (SeekableByteChannel channel = Files.newByteChannel(batchIdPath, StandardOpenOption.WRITE)) {
                    channel.write(buffer);
                }
            } else {
                writeBatchId(0, batchIdPath);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new RequestJournal(journal, flushContext, batchIdPath, id);
    }

    public int size() {
        return journal.size();
    }

    public boolean isEmpty() {
        return journal.isEmpty();
    }

    public void putRequest(NodeRequest request) {
        if(flushContext != null) {
            flushContext.put(request);
        } else {
            flushRequest(request);
        }
    }

    public void flush(NodeRequest request) {
        if(flushContext != null) {
            flushRequest(request);
            flushContext.remove(request);
        }
    }

    public void removeFirstRequestsBatch() {
        //TODO:
        //TODO: long overflow
        writeBatchId(++id, batchIdPath);
        journal.removeFirstBatch();
    }

    public List<NodeRequest> getRequestsBatch(int position) {
        List<NodeRequest> requests = new ArrayList<>();

        byte[] batch = getBatch(position);

        ByteBuffer buffer = ByteBuffer.wrap(batch);
        while(buffer.remaining() != 0) {
            int commandId = buffer.getInt();
            int argsCount = buffer.getInt();

            String[] args = new String[argsCount];
            for (int i = 0; i < argsCount; i++) {
                int argSize = buffer.getInt();
                byte[] argBytes = new byte[argSize];
                buffer.get(argBytes);
                String arg = bytesToUtf8String(argBytes);
                args[i] = arg;
            }

            Command command = Command.getCommandById(commandId);

            NodeRequest request = new NodeRequest(command, args);

            requests.add(request);
        }

        return requests;
    }

    public byte[] getBatch(int position) {
        byte[] batch = journal.getBatch(position);

        ByteBuffer buffer = ByteBuffer.allocate(8 + batch.length);
        buffer.putLong(id + position);
        buffer.put(batch);
        buffer.rewind();
        return buffer.array();
    }

    private void flushRequest(NodeRequest request) {
        final Command command = request.getCommand();
        final String[] args = request.getArgs();

        List<byte[]> argsBytes = new ArrayList<>();
        int size = 0;
        for(String arg : args) {
            byte[] bytes = utf8StringToBytes(arg);
            size += INTEGER_SIZE;
            size += bytes.length;
            argsBytes.add(bytes);
        }

        ByteBuffer buffer = ByteBuffer.allocate(INTEGER_SIZE + INTEGER_SIZE + size);

        buffer.putInt(command.getId());
        buffer.putInt(argsBytes.size());

        for(byte[] argBytes : argsBytes) {
            buffer.putInt(argBytes.length);
            buffer.put(argBytes);
        }

        buffer.flip();

        journal.write(buffer.array());
    }

    private static void writeBatchId(long id, Path path) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(id);
        buffer.flip();
        try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.WRITE)) {
            channel.write(buffer);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
