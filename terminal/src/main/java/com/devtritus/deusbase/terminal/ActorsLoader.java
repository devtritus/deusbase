package com.devtritus.deusbase.terminal;

import com.devtritus.deusbase.api.Command;
import com.devtritus.deusbase.api.NodeClient;
import com.devtritus.deusbase.api.NodeResponse;
import com.devtritus.deusbase.api.ProgramArgs;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import static com.devtritus.deusbase.api.ProgramArgNames.*;

class ActorsLoader {
    private final static int DEFAULT_ROW_COUNT = 10_000_000;

    static void load(String url, ProgramArgs programArgs) {

        int rowCount;
        if(programArgs.contains(ROW_COUNT)) {
            rowCount = programArgs.getInteger(ROW_COUNT);
        } else {
            rowCount = DEFAULT_ROW_COUNT;
        }

        String actorsFilePath = programArgs.get(ACTORS_FILE_PATH);

        NodeClient nodeClient = new NodeClient(url);

        try(Scanner scanner = new Scanner(new File(actorsFilePath), StandardCharsets.UTF_8.name())) {
            scanner.nextLine();

            int i = 0;
            while(scanner.hasNextLine() && i < rowCount) {
                i++;
                String line = scanner.nextLine();
                String[] tokens = line.split("\t");
                String[] args = new String[2];
                args[0] = tokens[1];
                args[1] = tokens[4];

                if(programArgs.contains("check")) {
                    NodeResponse response = nodeClient.request(Command.READ, args[0]);
                    List<String> values = response.getData().get(args[0]);
                    if(values == null || !values.contains(args[1])) {
                        throw new RuntimeException("Read error: " + args[0]);
                    }
                } else {
                    nodeClient.request(Command.CREATE, args);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
