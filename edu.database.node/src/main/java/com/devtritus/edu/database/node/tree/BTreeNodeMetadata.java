package com.devtritus.edu.database.node.tree;

class BTreeNodeMetadata {
    private Integer blocksCount;
    private Integer position;

    private Integer parentNodeId;

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

    public Integer getParentNodeId() {
        return parentNodeId;
    }

    public void setParentNodeId(Integer parentNodeId) {
        this.parentNodeId = parentNodeId;
    }
}
