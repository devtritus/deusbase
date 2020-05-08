package com.devtritus.deusbase.node;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class TestUtils {
    @SafeVarargs
    public static <T> List<T> listOf(T... elements) {
        return new ArrayList<>(Arrays.asList(elements));
    }

    public static List<String> getRandomStrings(int minLength, int maxLength, int count) {

        int leftLimit = 97; // 'a'
        int rightLimit = 122; // 'z'

        List<String> strings = new ArrayList<>();
        Random random = new Random();
        for(int i = 0; i < count; i++) {
            String generatedString = random.ints(leftLimit, rightLimit + 1)
                    .limit(minLength + random.nextInt(maxLength - minLength))
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();

            strings.add(generatedString);
        }

        return strings;
    }

    public static <T> List<T> getShuffledList(List<T> list) {
        List<T> result = new ArrayList<>(list);
        Collections.shuffle(list);
        return result;
    }


    public static List<Integer> getShuffledIntegerStream(int count) {
        List<Integer> integers = IntStream.range(0, count)
                .boxed()
                .collect(Collectors.toList());
        Collections.shuffle(integers);
        return integers;
    }

    public static <T> List<String> mapToStrings(List<T> list) {
        return list.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    public static Path createTempFile(String fileName) {
        try {
            return Files.createTempFile(fileName, null);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
