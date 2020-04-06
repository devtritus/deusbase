package com.devtritus.edu.database.core;

public class WrongArgumentsCountException extends Exception {
    public WrongArgumentsCountException(int min, int max, int actual) {
        super(String.format("Expected arguments count is from %s to %s, actual is %s", min, max, actual));
    }
}
