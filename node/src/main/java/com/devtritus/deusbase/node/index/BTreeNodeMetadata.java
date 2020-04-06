package com.devtritus.deusbase.node.index;

class BTreeNodeMetadata {
    private Integer blocksCount;
    private Integer position;

    Integer getBlockCount() {
        return blocksCount;
    }

    void setBlocksCount(Integer blockCount) {
        this.blocksCount = blockCount;
    }

    Integer getPosition() {
        return position;
    }

    void setPosition(Integer position) {
        this.position = position;
    }
}
