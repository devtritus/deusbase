package com.devtritus.deusbase.terminal;

import com.devtritus.deusbase.api.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

import static com.devtritus.deusbase.api.ProgramArgNames.ACTORS_FILE_PATH;
import static com.devtritus.deusbase.api.ProgramArgNames.ROW_COUNT;

class ActorsLoader1 {
    private final static int DEFAULT_ROW_COUNT = 10_000_000;
    private final static int REQUEST_BATCH_SIZE = 50000;

    static void load(String url, ProgramArgs programArgs) throws Exception {

        int rowCount;
        if (programArgs.contains(ROW_COUNT)) {
            rowCount = programArgs.getInteger(ROW_COUNT);
        } else {
            rowCount = DEFAULT_ROW_COUNT;
        }

        boolean checkMode = programArgs.contains("check");

        String actorsFilePath = programArgs.get(ACTORS_FILE_PATH);

        NodeClient nodeClient = new NodeClient(url);

        if (checkMode) {
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

        if (!checkMode) {
            System.out.println("Batch size: " + REQUEST_BATCH_SIZE);
            System.out.println("Number of batches: " + numberOfBatch);
        }

        System.out.println("Start time: " + startTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)));
        System.out.println();

        Set<String> titlesIds = new HashSet<>();
        try (Scanner scanner = new Scanner(new File("failed_ids"))) {
            while (scanner.hasNextLine()) {
                titlesIds.add(scanner.nextLine());
            }
        }

        try (Scanner scanner = new Scanner(new File(actorsFilePath), StandardCharsets.UTF_8.name())) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] nextTokens = line.split("\t");
                String nextId = nextTokens[0];

                if(titlesIds.contains(nextId)) {
                    NodeResponse response = nodeClient.request(Command.READ, nextId);
                    List<String> values = response.getData().get(nextId);
                    if(values == null || values.isEmpty()) {
                        //nodeClient.request(Command.CREATE, nextId, nextTokens[2]);
                    } else {
                        System.out.println(nextId);
                    }
                }
            }
        }
    }
}
