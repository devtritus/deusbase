package com.devtritus.deusbase.node.server;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.channels.ReadableByteChannel;
import java.util.Scanner;

public class JsonDataConverter {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T readNodeRequest(ReadableByteChannel channel, Class<T> object) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            Scanner scanner = new Scanner(channel);
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                stringBuilder.append(line);
            }

            return objectMapper.readValue(stringBuilder.toString(), object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] convertObjectToJsonBytes(Object object) {
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
