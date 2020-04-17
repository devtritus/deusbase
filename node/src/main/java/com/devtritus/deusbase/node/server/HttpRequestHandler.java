package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.api.*;
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

public class HttpRequestHandler extends AbstractHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RequestBodyHandler requestBodyHandler;

    public HttpRequestHandler(RequestBodyHandler requestBodyHandler) {
        this.requestBodyHandler = requestBodyHandler;
    }

    @Override
    public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
        String method = request.getMethod();

        httpServletResponse.setContentType("application/json;charset=UTF-8");

        if(!method.equals("POST")) {
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter writer = httpServletResponse.getWriter();
            writer.format("Request method type \"%s\" is unsupported", method);
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

            RequestBody requestBody = objectMapper.readValue(bodyMesssage, RequestBody.class);
            NodeResponse response = requestBodyHandler.handle(requestBody.getRequest());
            String responseBodyJson = objectMapper.writeValueAsString(response);

            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            httpServletResponse.getWriter().println(responseBodyJson);

            request.setHandled(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
