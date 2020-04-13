package com.devtritus.deusbase.terminal;

import com.devtritus.deusbase.api.NodeClient;
import com.devtritus.deusbase.api.ProgramArgs;
import com.devtritus.deusbase.api.ProgramArgsParser;

import static com.devtritus.deusbase.api.ProgramArgNames.DEBUG;
import static com.devtritus.deusbase.api.ProgramArgNames.URL;

public class Main {
    private final static String DEFAULT_CLIENT_URL = "http://127.0.0.1:7599";

    public static void main(String[] args) {
        ProgramArgs programArgs = ProgramArgsParser.parse(args);

        final String url = programArgs.getOrDefault(URL, DEFAULT_CLIENT_URL);
        final TerminalMode terminalMode = programArgs.contains(DEBUG) ? TerminalMode.DEBUG : TerminalMode.PROD;

        NodeClient nodeClient = new NodeClient(url);
        new Terminal(System.in, System.out, terminalMode, nodeClient).run();
    }
}
