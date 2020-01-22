package com.devtritus.edu.database.terminal;

import com.devtritus.edu.database.core.*;
import org.apache.http.conn.HttpHostConnectException;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    private final Client client;

    Terminal(InputStream in, PrintStream out, TerminalMode mode, Client client) {
        this.in = in;
        this.out = out;
        this.mode = mode;
        this.client = client;
    }

    void run() {
        if(mode == TerminalMode.DEBUG) {
            print(DEBUG_MODE);
        }
        print(HELLO);

        Scanner scanner = new Scanner(in);
        while(scanner.hasNextLine()) {
            String message = scanner.nextLine();
            String[] tokens = message.split("\\s+");
            String commandMessage = tokens[0];
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);

            if(commandMessage.trim().isEmpty()) {
                continue;
            }

            if(isSame(STOP, commandMessage)) {
                print(FINISH_MESSAGE);
                return;

            } else if(isSame(HELP, commandMessage)) {
                printAllCommands();

            } else if(isSystemCommand(commandMessage)) {
                handleSystemCommand(commandMessage, params);
            } else {
                print(WRONG_COMMAND + ": \"" + commandMessage + "\"");
            }
        }
    }

    private void handleSystemCommand(String message, String[] params) {
        try {
            Command command = Command.getCommand(message);

            CommandParamsValidator.validate(command, params);

            ResponseBody responseBody = client.request(command, params);

            ResponseStatus status = ResponseStatus.ofCode(responseBody.getCode());
            if(status != ResponseStatus.OK) {
                print(status.getMessage());
            } else {
                print(responseBody.getData());
            }
        } catch(HttpHostConnectException e) {
            print(SERVICE_UNAVAILABLE);
            print(e.getMessage());
        } catch (WrongArgumentsCountException e) {
            print(WRONG_ARGS_COUNT);
            print(e.getMessage());
        } catch (Exception e) {
            print(UNKNOWN_ERROR + ":");
            printStackTrace(e);
        }
    }

    private boolean isSystemCommand(String message) {
        return SYSTEM_COMMANDS.stream().anyMatch(command -> isSame(command, message));
    }

    private boolean isSame(String message, String command) {
        return command.equals(message);
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

    private void print(Map<String, String> map) {
        int i = 0;
        for(Map.Entry<String, String> entry : map.entrySet()) {
            if(map.size() > 1) {
                printInline(++i + ". ");
            }
            printInline(entry.getKey());
            printInline(" : ");
            printInline(entry.getValue());
            printInline("\n");
        }
    }

    private void printInline(String message) {
        out.print(message);
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
