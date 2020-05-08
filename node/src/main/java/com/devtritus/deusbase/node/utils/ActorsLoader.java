package com.devtritus.deusbase.node.utils;

import com.devtritus.deusbase.api.*;
import com.devtritus.deusbase.node.env.NodeEnvironment;
import com.devtritus.deusbase.node.server.CrudRequestHandler;
import com.devtritus.deusbase.node.server.NodeApi;
import com.devtritus.deusbase.node.storage.ValueStorage;
import com.devtritus.deusbase.node.tree.BTree;
import com.devtritus.deusbase.node.tree.BTreeInitializer;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import static com.devtritus.deusbase.api.ProgramArgNames.*;
import static com.devtritus.deusbase.api.ProgramArgNames.TREE_CACHE_LIMIIT;
import static com.devtritus.deusbase.node.env.NodeSettings.DEFAULT_TREE_CACHE_LIMIT;
import static com.devtritus.deusbase.node.env.NodeSettings.DEFAULT_TREE_M;

public class ActorsLoader {
    private final static int DEFAULT_ROW_COUNT = 10_000_000;

    public static void load(ProgramArgs programArgs) throws Exception {
        NodeEnvironment env = new NodeEnvironment();
        env.setUp(programArgs);

        int treeM = programArgs.getIntegerOrDefault(TREE_M, DEFAULT_TREE_M);
        int treeCacheLimit = programArgs.getIntegerOrDefault(TREE_CACHE_LIMIIT, DEFAULT_TREE_CACHE_LIMIT);

        BTree<String, List<Long>> tree = BTreeInitializer.init(env.getIndexPath(), treeM, treeCacheLimit);
        ValueStorage storage = new ValueStorage(env.getStoragePath());

        NodeApi nodeApi = new NodeApi(tree, storage);
        CrudRequestHandler requestHandler = new CrudRequestHandler();
        requestHandler.setApi(nodeApi);

        int rowCount;
        if(programArgs.contains(ROW_COUNT)) {
            rowCount = programArgs.getInteger(ROW_COUNT);
        } else {
            rowCount = DEFAULT_ROW_COUNT;
        }

        String dataFilePath = programArgs.get(DATA_FILE_PATH);

        try(Scanner scanner = new Scanner(new File(dataFilePath), StandardCharsets.UTF_8.name())) {
            scanner.nextLine();

            int i = 0;
            while(scanner.hasNextLine() && i < rowCount) {
                i++;
                String line = scanner.nextLine();
                String[] tokens = line.split("\t");
                String[] args = new String[2];
                args[0] = tokens[1];
                args[1] = tokens[4];

                requestHandler.handle(new NodeRequest(Command.CREATE, args));
            }
        }
    }
}
