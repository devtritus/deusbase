package com.devtritus.edu.database.node;

import com.devtritus.edu.database.core.RequestBody;
import com.devtritus.edu.database.core.RequestBodyHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class DatabaseRequestHandler extends AbstractHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RequestBodyHandler requestBodyHandler;

    public DatabaseRequestHandler(RequestBodyHandler requestBodyHandler) {
        this.requestBodyHandler = requestBodyHandler;
    }

    @Override
    public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
        String method = request.getMethod();

        if(!method.equals("POST")) {
            httpServletResponse.setStatus(500);
            PrintWriter writer = httpServletResponse.getWriter();
            writer.format("Request method type \"%s\" is unsupported", method);
            //TODO: сделать нормальную форму вывода при ошибках
            return;
        }

        BufferedReader reader = request.getReader();

        StringBuilder stringBuilder = new StringBuilder();
        Scanner scanner = new Scanner(reader);
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            stringBuilder.append(line);
        }

        try {
            String bodyMesssage = stringBuilder.toString();

            RequestBody body = objectMapper.readValue(bodyMesssage, RequestBody.class);

            requestBodyHandler.handle(body);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
