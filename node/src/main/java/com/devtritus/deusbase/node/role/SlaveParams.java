package com.devtritus.deusbase.node.role;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SlaveParams {
    private String address;
    private String uuid;
    private int position;
    private boolean online;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

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

    public boolean isOnline() {
        return online;
    }

    @JsonIgnore
    public void setOnline(boolean online) {
        this.online = online;
    }
}
