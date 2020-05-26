package com.devtritus.deusbase.terminal;

public enum TerminalMode {
    PROD,
    DEBUG,
    DATASET;

    public static TerminalMode fromText(String text) {
        for(TerminalMode nodeMode : TerminalMode.values()) {
            if(nodeMode.name().toLowerCase().equals(text)) {
                return nodeMode;
            }
        }

        throw new IllegalArgumentException(String.format("Unknown mode %s", text));
    }
}
