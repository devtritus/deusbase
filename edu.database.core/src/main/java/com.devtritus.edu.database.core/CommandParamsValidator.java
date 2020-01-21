package com.devtritus.edu.database.core;

public class CommandParamsValidator {
    public static void validate(Command command, String[] params) throws WrongArgumentsCountException {
        switch (command) {
            case CREATE:
            case UPDATE:
                assertParamsCount(params, 2);
                break;
            case READ:
            case DELETE:
                assertParamsCount(params, 1);
                break;
            default:
                throw new IllegalArgumentException("Unknown command: " + command);
        }
    }

    private static void assertParamsCount(String[] params, int expected) throws WrongArgumentsCountException {
        int actual = params.length;
        if(actual != expected) {
            throw new WrongArgumentsCountException(expected, actual);
        }
    }
}
