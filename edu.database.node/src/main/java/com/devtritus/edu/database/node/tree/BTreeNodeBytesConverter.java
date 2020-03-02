package com.devtritus.edu.database.node.tree;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

class BTreeNodeBytesConverter {

    static byte[] toBytes(BTreeNode node) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] nodeIdBytes = toByteArray(node.getNodeId());
        out.write(nodeIdBytes);

        byte[] levelBytes = toByteArray(node.getLevel());
        out.write(levelBytes);

        byte[] keysSizeBytes = toByteArray(node.getKeys().size());
        out.write(keysSizeBytes);

        for(String key : node.getKeys()) {
            byte[] keyLength = toByteArray(key.length());
            out.write(keyLength);

            out.write(key.getBytes(StandardCharsets.UTF_8));
        }

        for(int value : node.getValues()) {
            out.write(toByteArray(value));
        }

        byte[] childrenSizeBytes = toByteArray(node.getChildren().size());
        out.write(childrenSizeBytes);

        for(int children : node.getChildren()) {
            out.write(toByteArray(children));
        }

        return out.toByteArray();
    }

    static BTreeNode fromBytes(byte[] bytes) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        int nodeId = readInt(in);
        int level = readInt(in);
        int keysSize = readInt(in);

        BTreeNode node = new BTreeNode(nodeId, level);

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

        int childrenSize = readInt(in);

        for(int i = 0; i < childrenSize; i++) {
            int children = readInt(in);

            node.insertChildNode(i, children);
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
