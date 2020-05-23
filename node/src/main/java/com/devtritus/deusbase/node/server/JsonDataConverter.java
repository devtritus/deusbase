package com.devtritus.deusbase.node.server;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.Scanner;

public class JsonDataConverter {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    public static <T> List<T> readList(ReadableByteChannel channel, Class<T> elementType) {
        JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, elementType);

        try {
            String textBody = readTextBody(channel);
            return objectMapper.readValue(textBody, listType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readNodeRequest(ReadableByteChannel channel, Class<T> object) {
        try {
            String textBody = readTextBody(channel);
            return objectMapper.readValue(textBody, object);
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

    private static String readTextBody(ReadableByteChannel channel) {
        StringBuilder stringBuilder = new StringBuilder();
        Scanner scanner = new Scanner(channel);
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            stringBuilder.append(line);
        }

        return stringBuilder.toString();
    }
}
