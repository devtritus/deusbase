package com.devtritus.edu.database.terminal;

public class Main {
    public static void main(String[] args) throws Exception {
        TerminalMode mode = argsContains(TerminalMode.DEBUG.name(), args) ? TerminalMode.DEBUG : TerminalMode.PROD;

        new Terminal(System.in, System.out, mode).run();
    }

    private static boolean argsContains(String expectedArg, String[] args) {
        for(String arg : args) {
            if(arg.toLowerCase().startsWith(expectedArg.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
