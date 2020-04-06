package com.devtritus.deusbase.node.utils;

import com.devtritus.deusbase.core.Command;
import com.devtritus.deusbase.core.RequestBody;
import com.devtritus.deusbase.core.RequestBodyHandler;
import com.devtritus.deusbase.node.NodeApi;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ActorsLoader {
    public static void main(String[] args) throws Exception {
        ActorsLoader loader = new ActorsLoader();
        loader.load(10_000_000);
    }

    void load(int count) throws Exception {
        RequestBodyHandler handler = new RequestBodyHandler(new NodeApi("node.index", "value.storage"));

        try(Scanner scanner = new Scanner(new File("data.tsv"), StandardCharsets.UTF_8.name())) {
            scanner.nextLine();

            int i = 0;
            while(scanner.hasNextLine() && i < count) {
                i++;
                String line = scanner.nextLine();
                String[] tokens = line.split("\t");
                String[] args = new String[2];
                args[0] = tokens[1];
                args[1] = tokens[4];
                RequestBody body = new RequestBody();
                body.setCommand(Command.CREATE.toString());
                body.setArgs(args);
                handler.handle(body);
            }
        }
    }
}
