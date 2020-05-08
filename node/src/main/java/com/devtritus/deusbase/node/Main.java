package com.devtritus.deusbase.node;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.devtritus.deusbase.api.ProgramArgs;
import com.devtritus.deusbase.api.ProgramArgsParser;
import com.devtritus.deusbase.node.utils.ActorsLoader;
import com.devtritus.deusbase.node.utils.NodeMode;

import static com.devtritus.deusbase.api.ProgramArgNames.*;
import static com.devtritus.deusbase.node.env.NodeSettings.*;

public class Main {
    static {
        Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
    }

    public static void main(String[] args) throws Exception {
        ProgramArgs programArgs = ProgramArgsParser.parse(args);

        NodeMode mode = NodeMode.fromText(programArgs.getOrDefault(MODE, DEFAULT_NODE_MODE));
        if(mode == NodeMode.LOAD_DATA) {
            ActorsLoader.load(programArgs);
        } else {
            new Node(mode, programArgs).start();
        }
    }

    //TODO: design an initialization cycle
}
