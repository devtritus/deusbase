package com.devtritus.deusbase.node.env;

public interface NodeSettings {
    String DEFAULT_NODE_MODE = "master";
    String DEFAULT_ROOT_PATH = "./";
    String ROOT_DIRECTORY_NAME = "output";
    String DEFAULT_SCHEME_NAME = "default";
    String CONFIG_FILE_NAME = "config.json";
    String STORAGE_FILE_NAME = "storage.bin";
    String INDEX_FILE_NAME = "index.bin";

    int DEFAULT_TREE_M = 100;
    int DEFAULT_TREE_CACHE_LIMIT = 5000;

    String DEFAULT_HOST = "localhost";
    int DEFAULT_PORT = 4001;

    String DEFAULT_JOURNAL_PATH = "journal.bin";
    int DEFAULT_JOURNAL_BATCH_SIZE = 512 * 1024; //bytes
    int DEFAULT_JOURNAL_MIN_SIZE_TO_TRUNCATE = 8 * 1024 * 1024;

    String DEFAULT_FLUSH_CONTEXT_PATH = "flush_context.json";

    int DEFAULT_JETTY_MAX_THREADS = 20;
    int DEFAULT_JETTY_ACCEPT_QUEUE_SIZE = 100;
    String DEFAULT_ROUTER_CONFIG_PATH = "router_config.json";
}
