package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.api.*;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class HttpRequestHandler extends AbstractHandler {
    private final static Logger logger = LoggerFactory.getLogger(HttpRequestHandler.class);

    private final RequestHandler requestHandler;
    private final ExecutorService executorService;

    public HttpRequestHandler(RequestHandler requestHandler, ExecutorService executorService) {
        this.requestHandler = requestHandler;
        this.executorService = executorService;
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

        NodeResponse response;
        try {
            Future<NodeResponse> future = executorService.submit(() -> requestHandler.handle(command, channel));
            response = future.get();

            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response = new NodeResponse();
            response.setCode(ResponseStatus.SERVER_ERROR.getCode());
            response.setData("error", e.getMessage());

            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            logger.error("Internal server error", e);
        }

        byte[] byteResponse = JsonDataConverter.convertObjectToJsonBytes(response);

        request.setHandled(true);
        httpServletResponse.getOutputStream().write(byteResponse);
    }
}
