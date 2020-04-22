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

    private final Journal journal;
    private final FlushContext flushContext;

    private RequestJournal(Journal journal, FlushContext flushContext) {
        this.journal = journal;
        this.flushContext = flushContext;
    }

    public static RequestJournal init(Path journalPath, FlushContext flushContext, int batchSize, int minSizeToTruncate) {
        Journal journal = new Journal(journalPath, batchSize, minSizeToTruncate);
        journal.init();

        return new RequestJournal(journal, flushContext);
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
            flushContext.remove(request);
            flushRequest(request);
        }
    }

    public void removeFirstRequestsBatch() {
        journal.removeFirstBatch();
    }

    public byte[] getBatch(int position) {
        List<NodeRequest> requests = new ArrayList<>();

        return journal.getBatch(position);
    }

    public List<NodeRequest> getRequestsBatch(int position) {
        List<NodeRequest> requests = new ArrayList<>();

        byte[] batch = journal.getBatch(position);

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
}
