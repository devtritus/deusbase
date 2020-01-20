package com.devtritus.edu.database.terminal;

import java.util.Scanner;

class Terminal {
    void run() throws Exception {
        InputMessageHandler handler = new InputMessageHandler();
        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNextLine()) {
            String message = scanner.nextLine();
            if(message.equals("stop")) {
                System.out.println("Finish");
                return;
            }
            handler.handle(message);
        }
    }
}
