package com.devtritus.edu.database.core;

public class WrongArgumentsCountException extends Exception {
    public WrongArgumentsCountException(int expected, int actual) {
        super(String.format("expected: %s, actual: %s", expected, actual));
    }
}
