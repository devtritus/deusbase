package com.devtritus.deusbase.node.storage;

import com.devtritus.deusbase.api.Command;
import com.devtritus.deusbase.api.RequestBody;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.devtritus.deusbase.node.utils.Utils.bytesToUtf8String;
import static com.devtritus.deusbase.node.utils.Utils.utf8StringToBytes;

public class RequestJournal {
    private final static int INTEGER_SIZE = 4;

    private final Journal journal;

    private RequestJournal(Journal journal) {
        this.journal = journal;
    }

    public static RequestJournal create(Path path, int batchSize, int minSizeToTruncate) {
        Journal journal = new Journal(path, batchSize, minSizeToTruncate);
        journal.init();
        return new RequestJournal(journal);
    }

    public void putRequest(Command command, String[] args) {
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

    public List<RequestBody> getRequestsBatch() {
        List<RequestBody> requests = new ArrayList<>();

        byte[] batch = journal.getFirstBatch();

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

            RequestBody requestBody = new RequestBody();
            requestBody.setCommand(command.toString());
            requestBody.setArgs(args);

            requests.add(requestBody);
        }

        return requests;
    }

    public void removeFirstRequestsBatch() {
        journal.removeFirstBatch();
    }

    public boolean isEmpty() {
        return journal.isEmpty();
    }
}
