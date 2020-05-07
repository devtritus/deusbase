package com.devtritus.deusbase.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;

public class NodeClient {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final String url;

    public NodeClient(String url) {
        this.url = url;
    }

    public NodeResponse streamRequest(Command command, InputStream in) throws IOException {
        InputStreamEntity inputStreamEntity = new InputStreamEntity(in, -1, ContentType.APPLICATION_OCTET_STREAM);

        return doPost(command, inputStreamEntity);
    }

    public NodeResponse request(Command command, String... args) throws IOException {
        RequestBody requestBody = new RequestBody();
        requestBody.setArgs(args);
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        StringEntity entity = new StringEntity(jsonBody, ContentType.APPLICATION_JSON);

        return doPost(command, entity);
    }

    private NodeResponse doPost(Command command, HttpEntity entity) throws IOException {
        HttpPost httpPost = new HttpPost(url + "/" + command.toString());
        httpPost.setEntity(entity);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int status = response.getStatusLine().getStatusCode();

            HttpEntity responseEntity = response.getEntity();
            String entityMessage = responseEntity != null ? EntityUtils.toString(responseEntity) : null;

            if(entityMessage == null) {
                throw new IllegalStateException("Body is null");
            }

            if (status == 200) {
                return objectMapper.readValue(entityMessage, NodeResponse.class);
            } else {
                throw new ClientProtocolException(String.format("Unexpected response status: %s, details: %s", status, entityMessage));
            }
        }
    }

    @Override
    public String toString() {
        return "NodeClient{" +
                "url='" + url + '\'' +
                '}';
    }
}
