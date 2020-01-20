package com.devtritus.edu.database.core;

public enum Command {
    CREATE("create"),
    READ("read"),
    DELETE("delete"),
    UPDATE("update");

    private final String text;

    Command(String text) {
        this.text = text;
    }

    public static Command getCommand(String value) {
        for(Command command : Command.values()) {
            if(command.text.equals(value)) {
                return command;
            }
        }
        throw new IllegalArgumentException("Unexpected command: " + value);
    }

    @Override
    public String toString() {
        return text;
    }
}
