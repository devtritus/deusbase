package com.devtritus.deusbase.node.utils;

public enum NodeMode {
    MASTER,
    SLAVE,
    ROUTER;

    public static NodeMode fromText(String text) {
        for(NodeMode nodeMode : NodeMode.values()) {
            if(nodeMode.name().toLowerCase().equals(text)) {
                return nodeMode;
            }
        }

        throw new IllegalArgumentException(String.format("Unknown mode %s", text));
    }
}
