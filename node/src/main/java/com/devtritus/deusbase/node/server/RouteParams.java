package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.api.NodeClient;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class RouteParams {
    private NodeClient client;
    private boolean online;
    private int requestsCount;

    @JsonIgnore
    public NodeClient getClient() {
        return client;
    }

    public void setClient(NodeClient client) {
        this.client = client;
    }

    @JsonIgnore
    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public int getRequestsCount() {
        return requestsCount;
    }

    public void setRequestsCount(int requestsCount) {
        this.requestsCount = requestsCount;
    }

    @Override
    public String toString() {
        return "RouteParams{" +
                "client=" + client +
                ", online=" + online +
                ", requestsCount=" + requestsCount +
                '}';
    }
}
