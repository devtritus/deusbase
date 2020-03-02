package com.devtritus.edu.database.node.tree;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class BTreeIndexLoader {
        private final static int MIN_BLOCK_BYTE_SIZE = 512;
        private final File file;

        public BTreeIndexLoader(String filePath) {
            this.file = new File(filePath);
        }

        public boolean initialized() {
            return file.exists() && file.length() != 0;
        }

        public BTreeNodeDiskProvider load() {
            try (FileInputStream in = new FileInputStream(file)) {
                FileChannel fileChannel = in.getChannel();
                ByteBuffer headerBuffer = ByteBuffer.allocate(20);
                fileChannel.read(headerBuffer);
                headerBuffer.flip();
                int blockSize = headerBuffer.getInt();
                int m = headerBuffer.getInt();
                int rootPosition = headerBuffer.getInt();
                int lastPosition = headerBuffer.getInt();
                int lastNodeId = headerBuffer.getInt();
                fileChannel.position(rootPosition * blockSize);

                ByteBuffer blockBuffer = ByteBuffer.allocate(blockSize);

                fileChannel.read(blockBuffer);

                blockBuffer.flip();

                BTreeNodeDiskProvider nodeProvider = new BTreeNodeDiskProvider(m, blockSize, lastPosition, lastNodeId, file);
                nodeProvider.loadRoot(rootPosition);

                return nodeProvider;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    public BTreeNodeDiskProvider initialize(int m) {
        try {
            file.createNewFile();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try(FileChannel fileChannel = new FileOutputStream(file).getChannel()) {
            int blockSize = calculateBlockSize(m);
            System.out.println("BLOCKSIZE: " + blockSize);
            int lastPosition = 1;
            int lastNodeId = 1;
            int rootPosition = 1;

            ByteBuffer blockBuffer = ByteBuffer.allocate(blockSize);

            blockBuffer.putInt(blockSize)
                    .putInt(m)
                    .putInt(rootPosition)
                    .putInt(lastPosition)
                    .putInt(lastNodeId)
                    .rewind();

            fileChannel.write(blockBuffer);

            blockBuffer.clear();

            BTreeNode root = new BTreeNode(lastNodeId, 0);

            byte[] bytes = BTreeNodeBytesConverter.toBytes(root);

            blockBuffer.put(bytes)
                    .rewind();

            fileChannel.write(blockBuffer);

            BTreeNodeDiskProvider nodeProvider = new BTreeNodeDiskProvider(m, blockSize, lastPosition, lastNodeId, file);
            nodeProvider.loadRoot(rootPosition);

            return nodeProvider;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int calculateBlockSize(int m) {
        int minBlockSize = (32 + 12) * m;
        int blockSize = MIN_BLOCK_BYTE_SIZE;
        while((blockSize = blockSize * 2) < minBlockSize) {}
        return blockSize;
    }
}
