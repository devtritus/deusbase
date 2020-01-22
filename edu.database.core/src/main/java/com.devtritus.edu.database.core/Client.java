package com.devtritus.edu.database.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class Client {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final String url;

    public Client(String url) {
        this.url = url;
    }

    public ResponseBody request(Command command, String[] params) throws Exception {
        RequestBody requestBody = new RequestBody();
        requestBody.setCommand(command.toString());
        requestBody.setArgs(params);

        String body = objectMapper.writeValueAsString(requestBody);

        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int status = response.getStatusLine().getStatusCode();

            HttpEntity entity = response.getEntity();
            String entityMessage = entity != null ? EntityUtils.toString(entity) : null;

            if(entityMessage == null) {
                throw new IllegalStateException("Body is null");
            }

            if (status == 200) {
                return objectMapper.readValue(entityMessage, ResponseBody.class);
            } else {
                throw new ClientProtocolException(String.format("Unexpected response status: %s, details: %s", status, entityMessage));
            }
        }
    }
}
