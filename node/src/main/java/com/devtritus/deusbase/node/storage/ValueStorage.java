package com.devtritus.deusbase.node.storage;

import java.util.List;
import java.util.Map;

public interface ValueStorage {
    long write(String value);
    Map<Long, String> read(List<Long> addresses);
}