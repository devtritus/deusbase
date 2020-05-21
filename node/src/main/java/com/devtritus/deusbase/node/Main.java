package com.devtritus.deusbase.node;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.devtritus.deusbase.api.ProgramArgs;
import com.devtritus.deusbase.api.ProgramArgsParser;
import com.devtritus.deusbase.node.utils.NodeMode;

import static com.devtritus.deusbase.api.ProgramArgNames.*;
import static com.devtritus.deusbase.node.env.NodeSettings.*;

public class Main {

    public static void main(String[] args) {
        ProgramArgs programArgs = ProgramArgsParser.parse(args);

        Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        if(programArgs.contains("debug")) {
            root.setLevel(Level.DEBUG);
        } else {
            root.setLevel(Level.INFO);
        }

        NodeMode mode = NodeMode.fromText(programArgs.getOrDefault(MODE, DEFAULT_NODE_MODE));
        if(mode == NodeMode.ROUTER) {
            new Router(programArgs).start();
        } else {
            new Node(mode, programArgs).start();
        }
    }
}
