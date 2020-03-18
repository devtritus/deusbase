package com.devtritus.edu.database.node.tree;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

class BTreeNodeBytesConverter {

    static byte[] toBytes(BTreeNode node) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] nodeIdBytes = toByteArray(node.getNodeId());
        out.write(nodeIdBytes);

        out.write(node.getLevel());
        out.write(node.getKeys().size());

        for(String key : node.getKeys()) {
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            if(keyBytes.length > 254) { //size is 254 because children = (keySize + 1) and that value must be write to one byte
                throw new IllegalArgumentException("Key " + key + " is too long. Max size of key is 254 bytes");
            }

            out.write(keyBytes.length);
            out.write(keyBytes);
        }

        List<Entry<Integer, Integer>> indexToSizeValuesArray = new ArrayList<>();
        int valuesArrayCount = 0;
        for(int i = 0; i < node.getValues().size(); i++) {
            List<Long> values = node.getValues().get(i);
            if(values.size() > 1) {
                indexToSizeValuesArray.add(new Entry<>(i, values.size()));
                valuesArrayCount++;
            }
        }

        out.write(valuesArrayCount);

        for(int i = 0; i < valuesArrayCount; i++) {
            Entry<Integer, Integer> entry = indexToSizeValuesArray.get(i);
            out.write(entry.key);
            out.write(entry.value);
        }

        for(List<Long> values : node.getValues()) {
            for(Long value : values) {
                out.write(toByteArray(value));
            }
        }

        out.write(node.getChildren().size());

        for(int children : node.getChildren()) {
            out.write(toByteArray(children));
        }

        return out.toByteArray();
    }

    static BTreeNode fromBytes(byte[] bytes) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        int nodeId = readInt(in);

        int level = readByte(in);
        int keysSize = readByte(in);

        BTreeNode node = new BTreeNode(nodeId, level);

        List<String> keys = new ArrayList<>();
        for(int i = 0; i < keysSize; i++) {
            int keySize = readByte(in);
            String key = readString(keySize, in);

            keys.add(key);
        }

        Map<Integer, Integer> indexToSizeOfValuesArray = new HashMap<>();
        int valuesArrayCount = readByte(in);
        for(int i = 0; i < valuesArrayCount; i++) {
            int index = readByte(in);
            int size = readByte(in);
            indexToSizeOfValuesArray.put(index, size);
        }

        for(int i = 0; i < keysSize; i++) {
            Integer valuesArraySize = indexToSizeOfValuesArray.get(i);
            int size = valuesArraySize != null ? valuesArraySize : 1;

            List<Long> values = new ArrayList<>();
            for(int j = 0; j < size; j++) {
                values.add(readLong(in));
            }

            String key = keys.get(i);
            int index = node.searchKey(key);
            node.putKeyValue(index, key, values);
        }

        int childrenSize = readByte(in);

        for(int i = 0; i < childrenSize; i++) {
            int children = readInt(in);

            node.insertChildNode(i, children);
        }

        return node;
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
