package com.devtritus.deusbase.terminal;

import com.devtritus.deusbase.api.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;

import static com.devtritus.deusbase.api.ProgramArgNames.*;

class ActorsLoader {
    private final static int DEFAULT_ROW_COUNT = 10_000_000;
    private final static int REQUEST_BATCH_SIZE = 50000;

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

            String[] lastTokens = null;
            while(scanner.hasNextLine()) {
                Map<String, List<String[]>> idToTokens = new HashMap<>();
                String lastMask = null;

                while(scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] nextTokens = line.split("\t");
                    String nextId = nextTokens[0];
                    String nextMask = nextId.substring(0, nextId.length() - 1);
                    if(lastMask == null) {
                        lastMask = nextMask;
                    }

                    lastTokens = nextTokens;

                    if(!nextMask.equals(lastMask)) {
                       lastMask = nextMask;
                       break;
                    } else {
                       lastMask = nextMask;
                       List<String[]> tokensList1 = idToTokens.get(nextId);
                       if(tokensList1 == null) {
                           tokensList1 = new ArrayList<>();
                           idToTokens.put(nextId, tokensList1);
                       }
                       tokensList1.add(nextTokens);
                    }
                }

                for(Map.Entry<String, List<String[]>> entry : idToTokens.entrySet()) {

                    String[] tokens = getTokenByLanguagePriority(entry.getValue());
                    String[] args = new String[2];
                    args[0] = tokens[0];
                    args[1] = tokens[2];

                    nodeRequests.add(new NodeRequest(Command.CREATE, args));
                    requestBatchCounter++;

                    if (requestBatchCounter == REQUEST_BATCH_SIZE) {
                        nodeClient.executeRequests(nodeRequests);
                        requestBatchCounter = 0;
                        nodeRequests.clear();
                        System.out.print("\r" + ++batchCounter + " out of " + numberOfBatch + " batches are loaded");
                    }
                }

                idToTokens.clear();
                if(lastTokens != null) {
                    List<String[]> t = new ArrayList<>();
                    t.add(lastTokens);
                    idToTokens.put(lastTokens[0], t);
                }
            }

            if (!nodeRequests.isEmpty()) {
                nodeClient.executeRequests(nodeRequests);
                nodeRequests.clear();
                System.out.print("\r" + ++batchCounter + " out of " + numberOfBatch + " batches are loaded");
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

    private final static Map<String, Integer> languagesMap = new HashMap<String, Integer>() {{
        put("XWW", 0);
        put("CA", 1);
        put("GB", 2);
        put("JP", 3);
        put("IN", 4);
        put("PT", 5);
        put("DE", 6);
        put("RU", 7);
        put("ES", 8);
        put("IT", 9);
        put("FR", 10);
        put("\\N", 11);
        put("\\\\N", 12);
        put(" ", 13);
        put("US", 14);
    }};

    private final static Set<String> langset = new HashSet<>();

    private static String[] getTokenByLanguagePriority(List<String[]> tokensList) {
        String[] result = tokensList.get(0);
        int maxWeight = 0;

        for(String[] tokens : tokensList) {
            Integer tokenWeight = languagesMap.get(tokens[3]);
            langset.add(tokens[3]);
            if(tokenWeight == null) { }
            if(tokenWeight != null && tokenWeight > maxWeight) {
                maxWeight = tokenWeight;
                result = tokens;
            }
        }
        return result;
    }
}
