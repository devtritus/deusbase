package com.devtritus.edu.database.node.index;

import com.devtritus.edu.database.node.utils.Pair;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

abstract class BTreeNodeDataConverter {

    static byte[] toBytes(BTreeNodeData data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] nodeIdBytes = toByteArray(data.getNodeId());
        out.write(nodeIdBytes);

        out.write(data.getLevel());
        out.write(data.getKeys().size());

        for(String key : data.getKeys()) {
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            if(keyBytes.length > 254) { //size is 254 because children = (keySize + 1) and that value must be write to one byte
                throw new IllegalArgumentException("Key " + key + " is too long. Max size of key is 254 bytes");
            }

            out.write(keyBytes.length);
            out.write(keyBytes);
        }

        List<Pair<Integer, Integer>> indexToSizeValuesArray = new ArrayList<>();
        int valuesArrayCount = 0;
        for(int i = 0; i < data.getValues().size(); i++) {
            List<Long> values = data.getValues().get(i);
            if(values.size() > 1) {
                indexToSizeValuesArray.add(new Pair<>(i, values.size()));
                valuesArrayCount++;
            }
        }

        out.write(valuesArrayCount);

        for(int i = 0; i < valuesArrayCount; i++) {
            Pair<Integer, Integer> entry = indexToSizeValuesArray.get(i);
            out.write(entry.first);
            out.write(entry.second);
        }

        for(List<Long> values : data.getValues()) {
            for(Long value : values) {
                out.write(toByteArray(value));
            }
        }

        out.write(data.getChildrenPositions().size());

        for(int childPosition : data.getChildrenPositions()) {
            out.write(toByteArray(childPosition));
        }

        for(int childNodeId : data.getChildrenNodeIds()) {
            out.write(toByteArray(childNodeId));
        }

        return out.toByteArray();
    }

    static BTreeNodeData fromBytes(byte[] bytes) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        int nodeId = readInt(in);

        int level = readByte(in);
        int keysSize = readByte(in);

        BTreeNodeData data = new BTreeNodeData();
        data.setNodeId(nodeId);
        data.setLevel(level);

        List<String> keys = new ArrayList<>();
        for(int i = 0; i < keysSize; i++) {
            int keySize = readByte(in);
            String key = readString(keySize, in);

            keys.add(key);
        }

        data.setKeys(keys);

        Map<Integer, Integer> indexToSizeOfValuesArray = new HashMap<>();
        int valuesArrayCount = readByte(in);
        for(int i = 0; i < valuesArrayCount; i++) {
            int index = readByte(in);
            int size = readByte(in);
            indexToSizeOfValuesArray.put(index, size);
        }

        List<List<Long>> allValues = new ArrayList<>();
        for(int i = 0; i < keysSize; i++) {
            Integer valuesArraySize = indexToSizeOfValuesArray.get(i);
            int size = valuesArraySize != null ? valuesArraySize : 1;

            List<Long> values = new ArrayList<>();
            for(int j = 0; j < size; j++) {
                values.add(readLong(in));
            }

            allValues.add(values);
        }

        data.setValues(allValues);

        int childrenSize = readByte(in);

        List<Integer> childrenPositions = new ArrayList<>();
        for(int i = 0; i < childrenSize; i++) {
            int childPosition = readInt(in);

            childrenPositions.add(childPosition);
        }

        data.setChildrenPositions(childrenPositions);

        List<Integer> childrenNodeIds = new ArrayList<>();
        for(int i = 0; i < childrenSize; i++) {
            int childrenNodeId = readInt(in);

            childrenNodeIds.add(childrenNodeId);
        }

        data.setChildrenNodeIds(childrenNodeIds);

        return data;
    }

    private static int readByte(InputStream in) throws IOException {
        return in.read();
    }

    private static String readString(int length, InputStream in) throws IOException {
        byte[] bytes = new byte[length];

        in.read(bytes);

        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static long readLong(InputStream in) throws IOException {
        byte[] bytes = new byte[8];

        in.read(bytes);

        return toLongFromByteArray(bytes);
    }

    private static int readInt(InputStream in) throws IOException {
        byte[] bytes = new byte[4];

        in.read(bytes);

        return toIntFromByteArray(bytes);
    }

    private static byte[] toByteArray(long i) {
        return ByteBuffer.allocate(8).putLong(i).array();
    }

    private static byte[] toByteArray(int i) {
        return ByteBuffer.allocate(4).putInt(i).array();
    }

    private static int toIntFromByteArray(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    private static long toLongFromByteArray(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getLong();
    }
}
