package com.devtritus.deusbase.terminal;

import com.devtritus.deusbase.api.ProgramArgs;
import com.devtritus.deusbase.api.ProgramArgsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.devtritus.deusbase.api.ProgramArgNames.*;

public class Main {
    private final static Logger logger = LoggerFactory.getLogger(Main.class);
    private final static String DEFAULT_CLIENT_URL = "http://localhost:4001";

    public static void main(String[] args) throws Exception {
        ProgramArgs programArgs = ProgramArgsParser.parse(args);

        final String url = programArgs.getOrDefault(URL, DEFAULT_CLIENT_URL);

        TerminalMode mode;
        if(programArgs.contains(MODE)) {
            mode = TerminalMode.fromText(programArgs.get(MODE));
        } else {
            mode = TerminalMode.PROD;
        }

        if(mode == TerminalMode.ACTORS_LOADER) {
            ActorsLoader.load(url, programArgs);
        } else {
            logger.info("Attach terminal to {}", url);
            new Terminal(System.in, System.out, mode, url).run();
        }
    }
}
