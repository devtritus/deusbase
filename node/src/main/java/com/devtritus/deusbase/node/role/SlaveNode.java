package com.devtritus.deusbase.node.role;

import com.devtritus.deusbase.api.*;
import com.devtritus.deusbase.node.env.NodeEnvironment;
import org.apache.http.conn.HttpHostConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static com.devtritus.deusbase.node.utils.Utils.bytesToUtf8String;
import static com.devtritus.deusbase.node.utils.Utils.createFileIfNotExist;

public class SlaveNode implements SlaveApi {
    private final static Logger logger = LoggerFactory.getLogger(SlaveNode.class);

    private NodeEnvironment env;
    private Path path = Paths.get("slaveData.bin");
    private int batchSize;

    public SlaveNode(NodeEnvironment env, int batchSize) {
        this.env = env;
        createFileIfNotExist(path);
        this.batchSize = batchSize;
    }

    public void init(String slaveAddress, String masterAddress) {
        handshake(slaveAddress, masterAddress);
    }

    @Override
    public List<NodeRequest> receiveLogBatch(ReadableByteChannel channel) {
        //TODO: make buffer less
        ByteBuffer buffer = ByteBuffer.allocate(batchSize);
        try {
            channel.read(buffer);
        } catch(Exception e)  {
            throw new RuntimeException(e);
        }
        buffer.flip();
        Long batchId = buffer.getLong();
        try (SeekableByteChannel channel1 = Files.newByteChannel(path, StandardOpenOption.WRITE)) {
            ByteBuffer buffer1 = ByteBuffer.allocate(8).putLong(batchId);
            buffer1.rewind();
            channel1.write(buffer1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<NodeRequest> dd = new ArrayList<>();
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

            dd.add(request);
        }

        return dd;

    }

    private void handshake(String slaveAddress, String masterAddress) {
        NodeClient client = new NodeClient("http://" + masterAddress);
        String slaveUuid = env.getPropertyOrThrowException("uuid");
        List<String> argsList = new ArrayList<>();
        argsList.add(slaveAddress);
        argsList.add(slaveUuid);

        String state = env.getProperty("state");
        if(state == null || state.equals("init")) {
            argsList.add("init");
        } else if(state.equals("connect")) {
            argsList.add("connect");
            Long batchId = getLastBatchId();
            argsList.add(batchId.toString());
        }

        String[] args = argsList.toArray(new String[0]);

        NodeResponse response;
        try {
            response = client.request(Command.HANDSHAKE, args);
        } catch (HttpHostConnectException e) {
            logger.error("Master node through address {} is unavailable. Only READ mode is permitted", masterAddress);
            return;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //TODO: handler response from master and run server
        String actualMasterUuid = response.getData().get("result").get(0);
        String writtenMasterUuid = env.getProperty("masterUuid");
        if(writtenMasterUuid == null) { //first connection
            env.setProperty("masterUuid", actualMasterUuid);
        } else if(!actualMasterUuid.equals(writtenMasterUuid)) {
            throw new IllegalStateException(String.format("Slave isn't owned to master located at %s", masterAddress));
        }
    }

    public Long getLastBatchId() {
        try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(8);
            channel.read(buffer);
            buffer.rewind();
            return buffer.getLong();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
