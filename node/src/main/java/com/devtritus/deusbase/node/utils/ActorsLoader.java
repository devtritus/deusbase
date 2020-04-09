package com.devtritus.deusbase.node.utils;

import com.devtritus.deusbase.api.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static com.devtritus.deusbase.api.ProgramArgNames.DATA_FILE_PATH;
import static com.devtritus.deusbase.api.ProgramArgNames.ROW_COUNT;

public class ActorsLoader {
    private final static int DEFAULT_ROW_COUNT = 10_000_000;

    public static void load(ProgramArgs programArgs, RequestBodyHandler requestBodyHandler) throws Exception {
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
                RequestBody body = new RequestBody();
                body.setCommand(Command.CREATE.toString());
                body.setArgs(args);
                requestBodyHandler.handle(body);
            }
        }
    }
}
