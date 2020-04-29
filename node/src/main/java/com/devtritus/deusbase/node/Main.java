package com.devtritus.deusbase.node;

import com.devtritus.deusbase.api.ProgramArgs;
import com.devtritus.deusbase.api.ProgramArgsParser;
import com.devtritus.deusbase.node.env.NodeEnvironment;
import com.devtritus.deusbase.node.server.*;
import com.devtritus.deusbase.node.storage.ValueStorage;
import com.devtritus.deusbase.node.tree.BTree;
import com.devtritus.deusbase.node.tree.BTreeInitializer;
import com.devtritus.deusbase.node.utils.ActorsLoader;
import com.devtritus.deusbase.node.utils.NodeMode;

import java.util.List;

import static com.devtritus.deusbase.api.ProgramArgNames.*;
import static com.devtritus.deusbase.node.env.NodeSettings.*;

public class Main {
    public static void main(String[] args) throws Exception {
        ProgramArgs programArgs = ProgramArgsParser.parse(args);

        NodeMode mode = NodeMode.fromText(programArgs.getOrDefault(MODE, DEFAULT_NODE_MODE));
        if(mode == NodeMode.LOAD_DATA) {
            NodeEnvironment env = new NodeEnvironment();
            env.setUp(programArgs);

            int treeM = programArgs.getIntegerOrDefault(TREE_M, DEFAULT_TREE_M);
            int treeCacheLimit = programArgs.getIntegerOrDefault(TREE_CACHE_LIMIIT, DEFAULT_TREE_CACHE_LIMIT);

            BTree<String, List<Long>> tree = BTreeInitializer.init(env.getIndexPath(), treeM, treeCacheLimit);
            ValueStorage storage = new ValueStorage(env.getStoragePath());

            NodeApi nodeApi = new NodeApi(tree, storage);
            CrudRequestHandler requestHandler = new CrudRequestHandler();
            requestHandler.setApi(nodeApi);
            ActorsLoader.load(programArgs, requestHandler);
        } else {
            new Node(mode, programArgs).start();
        }
    }

    //TODO: design an initialization cycle
}
