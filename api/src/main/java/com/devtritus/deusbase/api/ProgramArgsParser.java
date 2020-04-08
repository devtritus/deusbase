package com.devtritus.deusbase.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ProgramArgsParser {
    public static ProgramArgs parse(String[] args) {
        Map<String, String> result = new HashMap<>();
        String lastKey = null;

        List<String> splittedArgs = Arrays.stream(args)
                .flatMap(arg -> Arrays.stream(arg.split("=")))
                .collect(Collectors.toList());

        for(String currentArg : splittedArgs) {
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
