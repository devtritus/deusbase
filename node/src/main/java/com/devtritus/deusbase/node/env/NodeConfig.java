package com.devtritus.deusbase.node.env;

import java.util.Map;

class NodeConfig {
    private Map<String, String> properties;

    Map<String, String> getProperties() {
        return properties;
    }

    void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
