package com.devtritus.deusbase.api;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class RequestBody {
    private String commandName;
    private String[] args;

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    @JsonIgnore
    public NodeRequest getRequest() {
        Command command = Command.getCommandByName(commandName);
        return new NodeRequest(command, args);
    }
}
