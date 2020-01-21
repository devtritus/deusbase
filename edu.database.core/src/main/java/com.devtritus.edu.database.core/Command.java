package com.devtritus.edu.database.core;

public enum Command {
    CREATE("create", CommandType.READ),
    READ("read", CommandType.WRITE),
    DELETE("delete", CommandType.WRITE),
    UPDATE("update", CommandType.WRITE);

    private final String text;
    private final CommandType type;

    Command(String text, CommandType type) {
        this.text = text;
        this.type = type;
    }

    public CommandType getType() {
        return type;
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
