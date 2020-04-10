package com.devtritus.deusbase.node;

import com.devtritus.deusbase.api.ProgramArgs;
import com.devtritus.deusbase.api.ProgramArgsParser;
import com.devtritus.deusbase.api.RequestBodyHandler;
import com.devtritus.deusbase.node.env.NodeEnvironment;
import com.devtritus.deusbase.node.server.*;
import com.devtritus.deusbase.node.utils.ActorsLoader;
import com.devtritus.deusbase.node.utils.NodeMode;

import static com.devtritus.deusbase.api.ProgramArgNames.*;
import static com.devtritus.deusbase.node.env.NodeSettings.*;

public class Main {
    public static void main(String[] args) throws Exception {
        ProgramArgs programArgs = ProgramArgsParser.parse(args);

        NodeEnvironment env = NodeEnvironment.getEnv(programArgs);

        NodeMode mode = NodeMode.fromText(programArgs.getOrDefault(MODE, DEFAULT_NODE_MODE));
        if(mode == NodeMode.LOAD_DATA) {
            RequestBodyHandler requestBodyHandler = new CrudRequestHandler(env.getNodeApi());
            ActorsLoader.load(programArgs, requestBodyHandler);
        } else {
            new Node(mode, env, programArgs).start();
        }
    }

    //TODO: design an initialization cycle
}
