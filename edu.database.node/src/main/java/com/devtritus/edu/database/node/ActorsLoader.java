package com.devtritus.edu.database.node;

import com.devtritus.edu.database.core.Command;
import com.devtritus.edu.database.core.RequestBody;
import com.devtritus.edu.database.core.RequestBodyHandler;
import java.io.File;
import java.util.Scanner;

public class ActorsLoader {
    public static void main(String[] args) throws Exception {
        ActorsLoader loader = new ActorsLoader();
        loader.load(1000000);
    }

    void load(int count) throws Exception {
        RequestBodyHandler handler = new RequestBodyHandler(new NodeApi());

        try(Scanner scanner = new Scanner(new File("data.tsv"), "UTF-8")) {
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
