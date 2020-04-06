package com.devtritus.edu.database.terminal;

import com.devtritus.edu.database.core.*;
import org.apache.http.conn.HttpHostConnectException;

import java.io.InputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
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
            String[] tokens = parse(message);
            String commandMessage = tokens[0];
            String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);

            if(commandMessage.trim().isEmpty()) {
                continue;
            }

            if(isSame(STOP, commandMessage)) {
                print(FINISH_MESSAGE);
                return;

            } else if(isSame(HELP, commandMessage)) {
                printAllCommands();

            } else if(isSystemCommand(commandMessage)) {
                handleSystemCommand(commandMessage, args);
            } else {
                print(WRONG_COMMAND + ": \"" + commandMessage + "\"");
            }
        }
    }

    private void handleSystemCommand(String message, String[] args) {
        try {
            Command command = Command.getCommand(message);

            CommandParamsUtils.handleParams(command, args);

            Instant start = Instant.now();

            ResponseBody responseBody = client.request(command, args);

            Instant finish = Instant.now();

            ResponseStatus status = ResponseStatus.ofCode(responseBody.getCode());
            if(status != ResponseStatus.OK) {
                print(status.getMessage());
            } else {
                print(responseBody.getData());
                print(Duration.between(start, finish).toMillis() + " ms");
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

    public static void main(String[] args) {
        System.out.println(Arrays.asList(parse("\"aaa\" bbb \"ccc\" ddd \"fff\"")));
    }

    private static String[] parse(String message) {
        List<String> result = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        boolean quotes = false;
        for(int i = 0; i < message.length(); i++) {
            char ch = message.charAt(i);
            if(!quotes && Character.isWhitespace(ch)) {
                if(builder.length() != 0) {
                    result.add(builder.toString());
                }
                builder = new StringBuilder();
            } else if (ch == '"') {
                if(quotes) {
                    quotes = false;
                    if(builder.length() != 0) {
                        result.add(builder.toString());
                        builder = new StringBuilder();
                    }
                } else {
                    quotes = true;
                }
            } else {
                builder.append(ch);
            }
        }

        if(builder.length() != 0) {
            result.add(builder.toString());
        }

        return result.toArray(new String[0]);
    }

    private void print() {
        print("");
    }

    private void print(Map<String, List<String>> map) {
        int i = 0;
        for(Map.Entry<String, List<String>> entry : map.entrySet()) {
            if(map.size() > 1) {
                printInline(++i + ". ");
            }
            printInline(entry.getKey());
            printInline(" : ");
            if(entry.getValue() != null) {
                if(entry.getValue().size() == 1) {
                    printInline(entry.getValue().get(0));
                    printInline("\n");
                } else {
                    printInline("\n");
                    for (int j = 0; j < entry.getValue().size(); j++) {
                        printInline("      " + j + ". " + entry.getValue().get(j) + "\n");
                    }
                    printInline("\n");
                }
            }
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
