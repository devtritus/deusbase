package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.api.*;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class HttpRequestHandler extends AbstractHandler {
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

        ReadableByteChannel channel = Channels.newChannel(request.getInputStream());

        Command command = Command.getCommandByName(s.substring(1));

        try {
            byte[] response = requestBodyHandler.handle(command, channel);

            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            httpServletResponse.getOutputStream().write(response);

            request.setHandled(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
