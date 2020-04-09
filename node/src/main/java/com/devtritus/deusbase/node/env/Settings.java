package com.devtritus.deusbase.node.env;

public interface Settings {
    String DATA_DIRECTORY_NAME = "data";
    String DEFAULT_SCHEME_NAME = "default";
    String CONFIG_FILE_NAME = "config.json";
    String STORAGE_FILE_NAME = "storage.bin";
    String INDEX_FILE_NAME = "index.bin";

    int DEFAULT_TREE_M = 100;
    int DEFAULT_TREE_CACHE_LIMIT = 5000;

    String DEFAULT_HOST = "127.0.0.1";
    int DEFAULT_PORT = 7599;
}
