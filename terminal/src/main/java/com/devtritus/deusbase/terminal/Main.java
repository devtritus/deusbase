package com.devtritus.deusbase.terminal;

import com.devtritus.deusbase.api.Client;
import java.util.Collections;
import java.util.Map;

public class Main {
    private final static String DEFAULT_URL = "http://127.0.0.1:7599";

    public static void main(String[] args) throws Exception {
        Client client = new Client(DEFAULT_URL);
        new Terminal(System.in, System.out, TerminalMode.DEBUG, client).run();
    }

    private static Map<String, String> parseArgs(String[] args) {
        //TODO:
        return Collections.emptyMap();
    }
}
