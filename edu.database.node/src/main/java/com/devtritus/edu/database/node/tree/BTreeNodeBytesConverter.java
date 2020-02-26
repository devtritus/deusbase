package com.devtritus.edu.database.node.tree;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

class BTreeNodeBytesConverter {

    static byte[] toBytes(BTreeNode<String, Integer> node) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] levelBytes = toByteArray(node.getLevel());
        out.write(levelBytes);

        byte[] keysSizeBytes = toByteArray(node.getKeysSize());
        out.write(keysSizeBytes);

        for(String key : node.getKeys()) {
            byte[] keyLength = toByteArray(key.length());
            out.write(keyLength);

            out.write(key.getBytes(StandardCharsets.UTF_8));
        }

        for(Integer value : node.getValues()) {
            out.write(toByteArray(value));
        }

        return out.toByteArray();
    }

    static BTreeNode<String, Integer> fromBytes(byte[] bytes) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        int level = readInt(in);
        int keysSize = readInt(in);

        BTreeNode<String, Integer> node = new BTreeNode<>(level);

        List<String> keys = new ArrayList<>();
        for(int i = 0; i < keysSize; i++) {
            int keySize = readInt(in);
            String key = readString(keySize, in);

            keys.add(key);
        }

        for(int i = 0; i < keysSize; i++) {
            int value = readInt(in);

            String key = keys.get(i);
            int index = node.searchKey(key);
            node.putKeyValue(index, key, value);
        }

        return node;
    }

    private static String readString(int length, InputStream in) throws IOException {
        byte[] bytes = new byte[length];

        in.read(bytes);

        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static int readInt(InputStream in) throws IOException {
        byte[] bytes = new byte[4];

        in.read(bytes);

        return toIntFromByteArray(bytes);
    }

    private static byte[] toByteArray(int i) {
        return ByteBuffer.allocate(4).putInt(i).array();
    }

    private static int toIntFromByteArray(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }
}
