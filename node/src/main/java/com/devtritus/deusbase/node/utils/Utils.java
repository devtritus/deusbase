package com.devtritus.deusbase.node.utils;

import com.devtritus.deusbase.node.Main;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public abstract class Utils {
    public static <T> List<T> insertToList(List<T> list, T element, int insertIndex)  {
        T current = element;
        for(int i = insertIndex; i < list.size(); i++) {
            current = list.set(i, current);
        }

        list.add(current);

        return list;
    }

    public static <T> List<T> deleteFromList(List<T> list, int deleteIndex) {
        if(list.isEmpty()) {
            return list;
        }

        for(int i = deleteIndex + 1; i < list.size(); i++) {
            T nextElement = list.get(i);
            list.set(i - 1, nextElement);
        }
        list.remove(list.size() - 1);

        return list;
    }

    public static byte[] utf8StringToBytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    public static String bytesToUtf8String(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void createFileIfNotExist(Path path) {
        if(!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Path createDirectoryIfNotExist(Path path) {
        try {
            if(!Files.exists(path)) {
                Files.createDirectory(path);
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        return path;
    }

    public static void deleteFileIfExists(Path path) {
        try {
            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Path createFile(Path path) {
        try {
            Files.createFile(path);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        return path;
    }

    public static void deleteFile(Path path) {
        try {
            Files.delete(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isEmptyFile(Path path) {
        try {
            return Files.size(path) == 0;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void printFromFile(String fileName) {
        try {
            InputStream in = Main.class.getClassLoader().getResourceAsStream(fileName);
            Scanner scanner = new Scanner(in);
            while(scanner.hasNextLine()) {
                System.out.println(scanner.nextLine());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
