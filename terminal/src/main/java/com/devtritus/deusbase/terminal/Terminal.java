package com.devtritus.deusbase.terminal;

import com.devtritus.deusbase.api.*;
import org.apache.http.conn.HttpHostConnectException;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.devtritus.deusbase.terminal.TerminalOutputMessages.*;

class Terminal {
    private final static List<String> EXTERNAL_COMMANDS = Arrays.stream(Command.externalCommands.toArray())
            .map(Object::toString)
            .collect(Collectors.toList());

    private final InputStream in;
    private final PrintStream out;
    private final TerminalMode mode;

    private String url;
    private NodeClient nodeClient;

    Terminal(InputStream in, PrintStream out, TerminalMode mode, String url) {
        this.in = in;
        this.out = out;
        this.mode = mode;
        this.url = url;
        this.nodeClient = new NodeClient(url);
    }

    void run() {
        if(mode == TerminalMode.DEBUG) {
            print(DEBUG_MODE);
        }
        print(HELLO);

        Scanner scanner = new Scanner(in);
        while(scanner.hasNextLine()) {
            String message = scanner.nextLine().trim();

            if(message.isEmpty()) {
                continue;
            }

            String[] tokens = parse(message);
            String commandMessage = tokens[0];
            String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);

            if(commandMessage.trim().isEmpty()) {
                continue;
            }

            if(isSame("exit", commandMessage)) {
                print(FINISH_MESSAGE);
                return;

            } else if(isSame("help", commandMessage)) {
                printHelp();

            } else if(isSame("connect", commandMessage)) {

                if(args.length != 1) {
                    print(WRONG_ARGS_COUNT);
                    print("Url to the database is expected");
                }
                String newUrl = args[0].startsWith("http://") ? args[0] : "http://" + args[0];
                if(!url.equals(newUrl)) {
                    url = newUrl;
                    nodeClient = new NodeClient(newUrl);
                }
                print("OK");

            } else if(isExternalCommand(commandMessage)) {
                handleExternalCommand(commandMessage, args);
            } else {
                print(WRONG_COMMAND + ": \"" + commandMessage + "\"");
            }
        }
    }

    private void handleExternalCommand(String message, String[] args) {
        try {
            Command command = Command.getCommandByName(message);

            args = CommandParamsUtils.handleParams(command, args);

            Instant start = Instant.now();

            NodeResponse response = nodeClient.request(command, args);

            Instant finish = Instant.now();

            ResponseStatus status = ResponseStatus.ofCode(response.getCode());
            if(status == ResponseStatus.OK) {
                print(response.getData());
                print(Duration.between(start, finish).toMillis() + " ms");
            } else if(status == ResponseStatus.SERVER_ERROR) {
                print(SERVER_ERROR);
                print(response.getData().get("error").get(0));
            } else {
                print(status.getMessage());
            }
        } catch(HttpHostConnectException e) {
            print(SERVICE_UNAVAILABLE);
            print(e.getMessage());
        } catch (WrongArgumentException e) {
            print(WRONG_ARGS_COUNT);
            print(e.getMessage());
        } catch (UnknownHostException e) {
            print(UNKNOWN_HOST + ": " + url);
        } catch (Exception e) {
            if(mode == TerminalMode.DEBUG) {
                print(UNKNOWN_ERROR + ":");
                printStackTrace(e);
            } else {
                print(UNKNOWN_ERROR + ": " + e.toString());
            }
        }
    }

    private boolean isExternalCommand(String message) {
        return EXTERNAL_COMMANDS.stream().anyMatch(command -> isSame(command, message));
    }

    private boolean isSame(String message, String command) {
        return command.equals(message);
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

    private static void printHelp() {
        try {
            InputStream in = Main.class.getClassLoader().getResourceAsStream("help.txt");
            Scanner scanner = new Scanner(in);
            while(scanner.hasNextLine()) {
                System.out.println(scanner.nextLine());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
