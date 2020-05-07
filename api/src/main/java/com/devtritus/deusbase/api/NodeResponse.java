package com.devtritus.deusbase.api;

import java.util.ArrayList;
import java.util.HashMap;
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

    public void setData(String key, String value) {
        List<String> values = new ArrayList<>();
        values.add(value);
        setData(key, values);
    }

    public void setData(String key, List<String> values) {
        data = new HashMap<>();
        data.put(key, values);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public static NodeResponse ok() {
        NodeResponse nodeResponse = new NodeResponse();
        nodeResponse.setCode(ResponseStatus.OK.getCode());
        return nodeResponse;
    }
}
