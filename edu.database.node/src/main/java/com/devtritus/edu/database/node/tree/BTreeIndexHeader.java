package com.devtritus.edu.database.node.tree;

import java.util.Objects;

class BTreeIndexHeader {
    final int blockSize;
    final int m;
    final int rootPosition;
    final int endPosition;
    final int lastNodeId;

    BTreeIndexHeader(int blockSize, int m, int rootPosition, int endPosition, int lastNodeId) {
        this.blockSize = blockSize;
        this.m = m;
        this.rootPosition = rootPosition;
        this.endPosition = endPosition;
        this.lastNodeId = lastNodeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BTreeIndexHeader that = (BTreeIndexHeader) o;
        return blockSize == that.blockSize &&
                m == that.m &&
                rootPosition == that.rootPosition &&
                endPosition == that.endPosition &&
                lastNodeId == that.lastNodeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockSize, m, rootPosition, endPosition, lastNodeId);
    }
}
