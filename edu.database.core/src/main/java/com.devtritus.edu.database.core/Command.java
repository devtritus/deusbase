package com.devtritus.edu.database.core;

public enum Command {
    READ("read", CommandType.READ, 1, 1),
    SEARCH("search", CommandType.READ, 1, 1),
    CREATE("create", CommandType.WRITE, 2, 2),
    DELETE("delete", CommandType.WRITE, 1, 2),
    UPDATE("update", CommandType.WRITE, 2, 3);

    private final String text;
    private final CommandType type;
    private final int minTokensNumber;
    private final int maxTokensNumber;

    Command(String text, CommandType type, int minTokensNumber, int maxTokensNumber) {
        this.text = text;
        this.type = type;
        this.minTokensNumber = minTokensNumber;
        this.maxTokensNumber = maxTokensNumber;
    }

    public void assertTokensNumber(int tokensNumber) {
        if(tokensNumber < minTokensNumber || maxTokensNumber < tokensNumber) {
            throw new IllegalArgumentException("Illegal number of arguments for command \"" + text + "\". " +
                    "Expected: [" + minTokensNumber + ", " + maxTokensNumber + "], actual: " + tokensNumber);
        }
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
