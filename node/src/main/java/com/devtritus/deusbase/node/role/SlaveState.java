package com.devtritus.deusbase.node.role;

public enum SlaveState {
    INIT("init"),
    CONNECT("connect");

    private String text;

    SlaveState(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static SlaveState fromText(String text) {
        for(SlaveState state : SlaveState.values()) {
            if(state.text.equals(text)) {
                return state;
            }
        }

        throw new IllegalStateException(String.format("Unknown state '%s'", text));
    }
}
