package com.devtritus.deusbase.node.storage;

import com.devtritus.deusbase.api.NodeRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FlushContext {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final File file;

    private List<NodeRequest> requests = new ArrayList<>();

    public FlushContext(Path path) {
        this.file = path.toFile();
    }

    public void put(NodeRequest request) {
        List<NodeRequest> requestsCopy = new ArrayList<>(requests);
        requestsCopy.add(request);
        updateContext(requestsCopy);
        requests.add(request);
    }

    public void remove(NodeRequest request) {
        List<NodeRequest> requestsCopy = new ArrayList<>(requests);
        requestsCopy.remove(request);
        updateContext(requestsCopy);
        requests.remove(request);
    }

    public List<NodeRequest> getAll() {
        if(requests.isEmpty()) {
            try {
                requests = objectMapper.readValue(file, new TypeReference<List<NodeRequest>>() {});
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return requests;
    }

    private void updateContext(List<NodeRequest> requests) {
        try {
            objectMapper.writeValue(file, requests);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
