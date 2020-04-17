package com.devtritus.deusbase.api;

import java.util.List;
import java.util.Map;

public class NodeResponse {
    private Map<String, List<String>> data;
    private int code;

    public Map<String, List<String>> getData() {
        return data;
    }

    public void setData(Map<String, List<String>> data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
