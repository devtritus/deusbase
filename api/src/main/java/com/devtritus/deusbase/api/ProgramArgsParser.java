package com.devtritus.deusbase.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class ProgramArgsParser {
    public static ProgramArgs parse(String[] args) {
        Map<String, String> result = new HashMap<>();
        String lastKey = null;
        for(String currentArg : args) {
            if(currentArg.startsWith("-")) {
                lastKey = currentArg.substring(1).toLowerCase();
                result.put(lastKey, null);
            } else if(lastKey != null){
                result.put(lastKey, currentArg.toLowerCase());
                lastKey = null;
            } else {
                throw new IllegalStateException(Arrays.toString(args));
            }
        }

        return new ProgramArgs(result);
    }
}
