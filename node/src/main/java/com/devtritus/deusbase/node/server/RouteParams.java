package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.api.NodeClient;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.concurrent.atomic.AtomicLong;

public class RouteParams {
    private NodeClient client;
    private volatile boolean online;
    private final AtomicLong requestsCount = new AtomicLong();

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

    public long getRequestsCount() {
        return requestsCount.get();
    }

    public void incrementRequestsCount() {
        requestsCount.incrementAndGet();
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
