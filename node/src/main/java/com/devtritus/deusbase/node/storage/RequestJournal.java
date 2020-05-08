package com.devtritus.deusbase.node.storage;

import com.devtritus.deusbase.api.Command;
import com.devtritus.deusbase.api.NodeRequest;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.devtritus.deusbase.node.utils.Utils.*;

public class RequestJournal {
    private final static int INTEGER_SIZE = 4;
    private final static int LONG_SIZE = 8;

    private final Journal journal;
    private long lastBatchId;

    private RequestJournal(Journal journal, long lastBatchId) {
        this.journal = journal;
        this.lastBatchId = lastBatchId;
    }

    public long getLastBatchId() {
        return lastBatchId;
    }

    public static RequestJournal init(Path journalPath, int batchSize, int minSizeToTruncate) {
        Journal journal = new Journal(journalPath, batchSize, minSizeToTruncate);
        journal.init();
        long lastBatchId = 0;
        int journalSize = journal.size();
        if(journalSize > 0) {
            byte[] lastBatch = journal.getBatch(journalSize - 1);
            ByteBuffer buffer = ByteBuffer.wrap(lastBatch);
            lastBatchId = buffer.getLong();
        }

        return new RequestJournal(journal, lastBatchId);
    }

    public int size() {
        return journal.size();
    }

    public boolean isEmpty() {
        return journal.isEmpty();
    }

    public void removeBatches(int end) {
        journal.removeBatches(end);
    }

    public void removeFirstBatch() {
        journal.removeFirstBatch();
    }

    public List<NodeRequest> getRequestsBatch(int position) {
        List<NodeRequest> requests = new ArrayList<>();

        byte[] batch = getBatch(position);

        ByteBuffer buffer = ByteBuffer.wrap(batch);
        long batchId = buffer.getLong();
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
        return journal.getBatch(position);
    }

    public void putRequest(NodeRequest request) {
        if(journal.isLastBatchEmpty()) {
            ByteBuffer longBuffer = ByteBuffer.allocate(LONG_SIZE)
                    .putLong(++lastBatchId);

            longBuffer.flip();

            journal.write(longBuffer.array());
        }

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
}
