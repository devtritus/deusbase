package com.devtritus.deusbase.terminal;

import com.devtritus.deusbase.api.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

class DatasetCreator {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static List<String> anyErrors = new ArrayList<>();
    static List<String> notFound = new ArrayList<>();
    static void create() throws Exception {
        File dataset = new File("my_dataset1.tsv");
        if(dataset.exists()) {
            dataset.delete();
        }
        dataset.createNewFile();

        NodeClient nodeClient = new NodeClient("http://localhost:4002");
        try(Scanner dataScanner = new Scanner(new File("data.tsv"), StandardCharsets.UTF_8.name());
            PrintWriter printWriter = new PrintWriter(dataset, StandardCharsets.UTF_8.name())) {
            dataScanner.nextLine();

            String lastId = null;
            while (dataScanner.hasNextLine()) {
                Map<String, Object> map = new LinkedHashMap<>();
                String line = dataScanner.nextLine();
                String[] tokens = line.split("\t");

                String id = tokens[0];
                if(lastId != null && lastId.equals(id)) {
                    continue;
                }
                String name = tokens[1];
                if(!tokens[2].equals("\\N") && !tokens[2].equals("\\\\N")) {
                    map.put("birth", tokens[2]);
                }
                if (!tokens[3].equals("\\N") && !tokens[3].equals("\\\\N")) {
                    map.put("death", tokens[3]);
                }
                if (tokens[4] != null && !tokens[4].isEmpty()) {
                    String[] professions = tokens[4].split(",");
                    if(professions.length > 0) {
                        map.put("prof", Arrays.asList(professions));
                    }
                }
                if (tokens[5] != null) {
                    String[] titleIds = tokens[5].split(",");

                    List<String> titles = new ArrayList<>();
                    for (String titleId : titleIds) {
                        if(titleId.equals("\\N")) {
                            continue;
                        }
                        NodeResponse response = nodeClient.request(Command.READ, titleId);
                        List<String> values = response.getData().get(titleId);
                        if(values == null) {
                            notFound.add(titleId);
                        } else {
                            titles.add(values.get(0));
                        }
                    }
                    if(!titles.isEmpty()) {
                        map.put("titles", titles);
                    }
                }

                printWriter.write(name);
                printWriter.write("\t");

                if(!map.isEmpty()) {
                    String value = objectMapper.writeValueAsString(map);
                    printWriter.write(value);
                }

                printWriter.write("\n");

                lastId = id;
            }

            printWriter.write(objectMapper.writeValueAsString(notFound + "\n"));
            printWriter.write(objectMapper.writeValueAsString(anyErrors + "\n"));
        }

    }
}
