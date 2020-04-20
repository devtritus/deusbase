package com.devtritus.deusbase.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Objects;

public class NodeRequest {
    private Command command;
    private String[] args;

    @JsonCreator
    public NodeRequest(@JsonProperty("command") Command command, @JsonProperty("args") String[] args) {
        this.command = command;
        this.args = args;
    }

    public Command getCommand() {
        return command;
    }

    public String[] getArgs() {
        return args;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeRequest that = (NodeRequest) o;
        return Objects.equals(command, that.command) &&
                Arrays.equals(args, that.args);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(command);
        result = 31 * result + Arrays.hashCode(args);
        return result;
    }

    @Override
    public String toString() {
        return "RequestBody{" +
                "command='" + command + '\'' +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}
