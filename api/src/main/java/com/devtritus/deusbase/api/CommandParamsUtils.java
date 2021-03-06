package com.devtritus.deusbase.api;

import java.util.Arrays;

public class CommandParamsUtils {
    public static String[] handleParams(Command command, String[] params) throws WrongArgumentException {
        command.assertTokensNumber(params.length);

        switch (command) {
            case UPDATE:
                if(params.length == 2) {
                    return Arrays.asList(params[0], "0", params[1]).toArray(new String[0]);
                }
                break;
        }

        return params;
    }
}
