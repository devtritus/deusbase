package com.devtritus.deusbase.api;

public enum ResponseStatus {
    OK(0, "ok"),
    NOT_FOUND(1, "Not found");

    private int code;
    private String message;

    ResponseStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static ResponseStatus ofCode(int code) {
        for(ResponseStatus responseStatus : ResponseStatus.values()) {
            if(responseStatus.getCode() == code) {
                return responseStatus;
            }
        }
        throw new IllegalArgumentException(String.format("Unknown status code: %s", code));
    }
}
