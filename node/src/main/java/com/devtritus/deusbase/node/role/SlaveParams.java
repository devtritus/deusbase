package com.devtritus.deusbase.node.role;

import com.devtritus.deusbase.api.NodeClient;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class SlaveParams {
    private String uuid;
    private int position;
    private NodeClient client;
    private boolean online;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

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

    @Override
    public String toString() {
        return "SlaveParams{" +
                "uuid='" + uuid + '\'' +
                ", position=" + position +
                ", client=" + getClient() +
                ", online=" + isOnline() +
                '}';
    }
}
