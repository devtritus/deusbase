package com.devtritus.edu.database.node.tree;

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
