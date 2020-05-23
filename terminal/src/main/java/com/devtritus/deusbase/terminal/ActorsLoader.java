package com.devtritus.deusbase.terminal;

import com.devtritus.deusbase.api.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.devtritus.deusbase.api.ProgramArgNames.*;

class ActorsLoader {
    private final static int DEFAULT_ROW_COUNT = 10_000_000;
    private final static int REQUEST_BATCH_SIZE = 5000;

    static void load(String url, ProgramArgs programArgs) throws Exception {

        int rowCount;
        if(programArgs.contains(ROW_COUNT)) {
            rowCount = programArgs.getInteger(ROW_COUNT);
        } else {
            rowCount = DEFAULT_ROW_COUNT;
        }

        boolean checkMode = programArgs.contains("check");

        String actorsFilePath = programArgs.get(ACTORS_FILE_PATH);

        NodeClient nodeClient = new NodeClient(url);

        if(checkMode) {
            System.out.println("DATASET CHECKING");
            System.out.println();
            System.out.format("Number of rows for checking: %s\n", rowCount);
        } else {
            System.out.println("DATASET LOADING");
            System.out.println();
            System.out.format("Number of rows for loading: %s\n", rowCount);
        }

        LocalDateTime startTime = LocalDateTime.now();

        int numberOfBatch = rowCount / REQUEST_BATCH_SIZE;

        if(!checkMode) {
            System.out.println("Batch size: " + REQUEST_BATCH_SIZE);
            System.out.println("Number of batches: " + numberOfBatch);
        }

        System.out.println("Start time: " + startTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)));
        System.out.println();

        try(Scanner scanner = new Scanner(new File(actorsFilePath), StandardCharsets.UTF_8.name())) {
            scanner.nextLine();

            List<NodeRequest> nodeRequests = new ArrayList<>();
            int requestBatchCounter = 0;
            int batchCounter = 0;
            int i = 0;

            while(i++ < rowCount) {
                boolean hasNextLine = scanner.hasNextLine() && i < rowCount;

                String[] args = null;
                if(hasNextLine) {
                    String line = scanner.nextLine();
                    String[] tokens = line.split("\t");
                    args = new String[2];
                    args[0] = tokens[1];
                    args[1] = tokens[4];
                }

                if(checkMode && hasNextLine) {
                    NodeResponse response = nodeClient.request(Command.READ, args[0]);
                    List<String> values = response.getData().get(args[0]);
                    if(values == null || !values.contains(args[1])) {
                        throw new RuntimeException("Read error: " + args[0]);
                    }
                } else if(!checkMode) {
                    if(hasNextLine) {
                        nodeRequests.add(new NodeRequest(Command.CREATE, args));
                        requestBatchCounter++;
                    }
                    if(requestBatchCounter > REQUEST_BATCH_SIZE || !hasNextLine) {
                        nodeClient.executeRequests(nodeRequests);
                        requestBatchCounter = 0;
                        nodeRequests.clear();
                        System.out.print("\r" + ++batchCounter + " out of " + numberOfBatch + " batches are loaded");
                    }
                }
            }
        }

        if(!checkMode) {
            System.out.print("\n\n");
        } else {
            System.out.println("All data exists");
        }

        System.out.println();

        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = Duration.between(startTime, endTime);
        System.out.println("End time: " + endTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)));
        System.out.println("Time spent: " + LocalTime.MIDNIGHT.plus(duration).format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
}
