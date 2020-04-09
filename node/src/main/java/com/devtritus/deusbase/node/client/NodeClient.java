package com.devtritus.deusbase.node.client;

import com.devtritus.deusbase.api.ProgramArgs;
import com.devtritus.deusbase.node.env.NodeEnvironment;
import com.devtritus.deusbase.node.utils.NodeMode;

public class NodeClient implements Runnable  {
    private final NodeMode mode;
    private final NodeEnvironment env;
    private final ProgramArgs programArgs;

    public NodeClient(NodeMode mode, NodeEnvironment env, ProgramArgs programArgs) {
        this.mode = mode;
        this.env = env;
        this.programArgs = programArgs;
    }

    @Override
    public void run() {

    }
}
