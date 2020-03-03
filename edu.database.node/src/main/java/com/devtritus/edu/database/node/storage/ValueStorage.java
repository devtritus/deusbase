package com.devtritus.edu.database.node.storage;

import java.util.List;
import java.util.Map;

public interface ValueStorage {
    long put(String value);
    Map<Long, String> get(List<Long> addresses);
}
