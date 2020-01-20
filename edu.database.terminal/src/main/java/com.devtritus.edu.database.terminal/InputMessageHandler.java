package com.devtritus.edu.database.terminal;

import com.devtritus.edu.database.core.Command;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

class InputMessageHandler {
    public void handle(String message) throws Exception {
        Command command = Command.getCommand(message);
        send(command);
    }

    private void send(Command command) throws Exception {
        switch (command) {
            case READ:
                get(command);
                break;
            default:
                post(command);
                break;
        }
    }

    private void get(Command command) throws Exception {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet("http://127.0.0.1:7599?command=" + command.toString());

            System.out.println("Executing get request " + httpGet.getRequestLine());

            String responseBody = httpclient.execute(httpGet, response -> {
                int status = response.getStatusLine().getStatusCode();
                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            });
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
        }

    }

    private void post(Command command) throws Exception {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost("http://127.0.0.1:7599");
            httpPost.setEntity(new StringEntity(command.toString(), ContentType.APPLICATION_JSON));

            System.out.println("Executing post request " + httpPost.getRequestLine());

            String responseBody = httpclient.execute(httpPost, response -> {
                int status = response.getStatusLine().getStatusCode();
                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            });
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
        }
    }
}
