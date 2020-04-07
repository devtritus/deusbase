package com.devtritus.deusbase.terminal;

import com.devtritus.deusbase.api.Client;
import com.devtritus.deusbase.api.ProgramArgs;
import com.devtritus.deusbase.api.ProgramArgsParser;

public class Main {
    private final static String DEFAULT_URL = "http://127.0.0.1:7599";

    public static void main(String[] args) {
        ProgramArgs programArgs = ProgramArgsParser.parse(args);

        final String url = programArgs.getOrDefault("url", DEFAULT_URL);
        final TerminalMode terminalMode = programArgs.contains("debug") ? TerminalMode.DEBUG : TerminalMode.PROD;

        Client client = new Client(url);
        new Terminal(System.in, System.out, terminalMode, client).run();
    }
}
