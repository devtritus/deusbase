package com.devtritus.edu.database.terminal;

import com.devtritus.edu.database.core.Command;
import org.apache.http.conn.HttpHostConnectException;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.devtritus.edu.database.terminal.TerminalOutputMessages.*;

class Terminal {
    private final static List<String> SYSTEM_COMMANDS = Arrays.stream(Command.values())
            .map(Object::toString)
            .collect(Collectors.toList());

    private final InputStream in;
    private final PrintStream out;
    private final TerminalMode mode;

    Terminal(InputStream in, PrintStream out, TerminalMode mode) {
        this.in = in;
        this.out = out;
        this.mode = mode;
    }

    void run() throws Exception {
        if(mode == TerminalMode.DEBUG) {
            print(DEBUG_MODE);
        }
        print(HELLO);

        InputMessageHandler handler = new InputMessageHandler();
        Scanner scanner = new Scanner(in);
        while(scanner.hasNextLine()) {
            String message = scanner.nextLine();

            if(isSame(STOP, message)) {
                print(FINISH_MESSAGE);

            } else if(isSame(HELP, message)) {
                printAllCommands();

            } else if(isSystemCommand(message)) {
                handleSystemCommand(message, handler);
            } else {
                print(UNKNOWN_COMMAND + ": \"" + message + "\"");
            }
        }
    }

    private void handleSystemCommand(String message, InputMessageHandler handler) {
        try {
            handler.handle(message);
        } catch(HttpHostConnectException e) {
            print(SERVICE_UNAVAILABLE);
            printStackTrace(e);
        } catch (Exception e) {
            print(UNKNOWN_ERROR + ":");
            printStackTrace(e);
        }
    }

    private boolean isSame(String message, String command) {
        return command.equals(message);
    }

    private boolean isSystemCommand(String message) {
        return SYSTEM_COMMANDS.stream().anyMatch(message::startsWith);
    }

    private void printAllCommands() {
        print();
        print(AVAILABLE_COMMANDS);
        List<String> allCommands = getAllCommands();
        print();
        for(String command : allCommands) {
            print(command);
        }
        print();
    }

    private List<String> getAllCommands() {
        return Stream.concat(
                    SYSTEM_COMMANDS.stream(),
                    Stream.of(STOP, HELP))
                .collect(Collectors.toList());
    }

    private void print() {
        print("");
    }

    private void print(String message) {
        out.println(message);
    }

    private void printStackTrace(Exception e) {
        if(mode == TerminalMode.DEBUG) {
            e.printStackTrace();
        }
    }
}
