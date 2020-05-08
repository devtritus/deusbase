package com.devtritus.deusbase.terminal;

import com.devtritus.deusbase.api.ProgramArgs;
import com.devtritus.deusbase.api.ProgramArgsParser;

import static com.devtritus.deusbase.api.ProgramArgNames.*;

public class Main {
    private final static String DEFAULT_CLIENT_URL = "http://127.0.0.1:3334";

    public static void main(String[] args) {
        ProgramArgs programArgs = ProgramArgsParser.parse(args);

        final String url = programArgs.getOrDefault(URL, DEFAULT_CLIENT_URL);
        final TerminalMode terminalMode = programArgs.contains(DEBUG) ? TerminalMode.DEBUG : TerminalMode.PROD;

        System.out.println("Attach terminal to " + url);

        new Terminal(System.in, System.out, terminalMode, url).run();
        //TODO: add connection checking query
    }
}
