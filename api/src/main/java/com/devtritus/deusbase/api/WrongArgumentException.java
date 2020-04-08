package com.devtritus.deusbase.api;

public class WrongArgumentException extends Exception {
    public WrongArgumentException(int min, int max, int actual) {
        super(String.format("Expected arguments count is from %s to %s, actual is %s", min, max, actual));
    }
}
