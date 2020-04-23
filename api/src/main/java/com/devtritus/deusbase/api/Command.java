package com.devtritus.deusbase.api;

public enum Command {
    READ(1, "read", CommandType.READ, 1, 1),
    SEARCH(2, "search", CommandType.READ, 1, 1),
    CREATE(3, "create", CommandType.WRITE, 2, 2),
    DELETE(4, "delete", CommandType.WRITE, 1, 2),
    UPDATE(5,"update", CommandType.WRITE, 2, 3),

    HANDSHAKE(6,"handshake", CommandType.WRITE, 2, 2),
    BATCH(7, "batch", CommandType.WRITE, 2, 2),
    COPY(8, "copy", CommandType.WRITE, 0, 0);

    private final int id;
    private final String text;
    private final CommandType type;
    private final int minTokensNumber;
    private final int maxTokensNumber;

    Command(int id, String text, CommandType type, int minTokensNumber, int maxTokensNumber) {
        this.id = id;
        this.text = text;
        this.type = type;
        this.minTokensNumber = minTokensNumber;
        this.maxTokensNumber = maxTokensNumber;
    }

    public void assertTokensNumber(int tokensNumber) throws WrongArgumentException {
        if(tokensNumber < minTokensNumber || maxTokensNumber < tokensNumber) {
            throw new WrongArgumentException(minTokensNumber, maxTokensNumber, tokensNumber);
        }
    }

    public int getId() {
        return id;
    }

    public CommandType getType() {
        return type;
    }

    public static Command getCommandByName(String value) {
        for(Command command : Command.values()) {
            if(command.text.equals(value)) {
                return command;
            }
        }
        throw new IllegalArgumentException("Unexpected command: " + value);
    }

    public static Command getCommandById(int id) {
        for(Command command : Command.values()) {
            if(command.id == id) {
                return command;
            }
        }
        throw new IllegalArgumentException(String.format("Id %s doesn't exist", id));
    }

    @Override
    public String toString() {
        return text;
    }
}
