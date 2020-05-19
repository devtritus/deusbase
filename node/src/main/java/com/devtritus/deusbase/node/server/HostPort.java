package com.devtritus.deusbase.node.server;

public class HostPort {
    public String host;
    public int port;

    public String getHttpUrl() {
        return "http://" + host + ":" + port;
    }
}
