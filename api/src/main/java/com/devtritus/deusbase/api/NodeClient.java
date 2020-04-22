package com.devtritus.deusbase.api;

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

public class NodeClient {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final String url;

    public NodeClient(String url) {
        this.url = url;
    }

    public NodeResponse sneakyRequest(Command command, String... args) {
        try {
            return doRequest(command, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public NodeResponse request(Command command, String... args) throws Exception {
        return doRequest(command, args);
    }

    private NodeResponse doRequest(Command command, String[] args) throws Exception {
        RequestBody requestBody = new RequestBody();
        requestBody.setArgs(args);

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpPost httpPost = new HttpPost(url + "/" + command.toString());
        httpPost.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int status = response.getStatusLine().getStatusCode();

            HttpEntity entity = response.getEntity();
            String entityMessage = entity != null ? EntityUtils.toString(entity) : null;

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
}
