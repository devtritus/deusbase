package com.devtritus.deusbase.node.server;

import com.devtritus.deusbase.api.Api;
import com.devtritus.deusbase.node.storage.ValueStorage;
import com.devtritus.deusbase.node.tree.*;

import java.util.*;
import java.util.stream.Collectors;

public class NodeApi implements Api<String, String> {
    private BTree<String, List<Long>> tree;
    private ValueStorage storage;

    public NodeApi() {}

    public NodeApi(BTree<String, List<Long>> tree, ValueStorage storage) {
        this.tree = tree;
        this.storage = storage;
    }

    @Override
    public Map<String, List<String>> read(String key) {
        List<Long> addresses = tree.searchByKey(key);

        if(addresses != null) {
            Map<Long, String> addressToValueMap = storage.read(addresses);

            List<String> values = addresses.stream()
                    .map(addressToValueMap::get)
                    .collect(Collectors.toList());

            return Collections.singletonMap(key, values);
        }

        return Collections.emptyMap();
    }

    @Override
    public Map<String, List<String>> search(String key) {
        Map<String, List<Long>> fetchResult = tree.fetch(key);

        List<Long> allAddresses = fetchResult.values().stream()
                .flatMap(List::stream)
                .sorted()
                .collect(Collectors.toList());

        Map<Long, String> addressToValueMap = storage.read(allAddresses);

        Map<String, List<String>> result = new HashMap<>();
        for (Map.Entry<String, List<Long>> entry : fetchResult.entrySet()) {
            String fetchedKey = entry.getKey();
            List<Long> fetchedAddresses = entry.getValue();

            List<String> values;
            if(fetchedAddresses != null) {

                values = fetchedAddresses.stream()
                        .map(addressToValueMap::get)
                        .collect(Collectors.toList());
            } else {
                throw new RuntimeException();
            }

            result.put(fetchedKey, values);
        }

        return result;
    }

    @Override
    public Map<String, List<String>> create(String key, String value) {
        List<Long> addresses = tree.searchByKey(key);
        long address = storage.write(value);

        if (addresses != null) { //if addresses is not null then addresses collection must contains at least one element
            addresses.add(address);
            tree.add(key, addresses);
        } else {
            tree.add(key, new ArrayList<>(Collections.singletonList(address)));
        }

        return Collections.singletonMap(key, Collections.singletonList(value));
    }

    @Override
    public Map<String, List<String>> update(String key, int valueIndex, String value) {
        List<Long> addresses = tree.searchByKey(key);

        if(addresses != null) {
            long address = storage.write(value);
            addresses.set(valueIndex, address);
            tree.add(key, addresses);
        } else {
            create(key, value);
        }

        return Collections.singletonMap(key, Collections.singletonList(value));
    }

    @Override
    public Map<String, List<String>> delete(String key) {
        List<Long> addresses = tree.searchByKey(key);

        if (addresses != null) {
            tree.deleteKey(key);
        }

        return Collections.singletonMap(key, null);
    }

    @Override
    public Map<String, List<String>> delete(String key, int valueIndex) {
        List<Long> addresses = tree.searchByKey(key);

        if (addresses != null) {
            if (addresses.size() == 1) {
                tree.deleteKey(key);
            } else {
                addresses.remove(valueIndex);
                tree.add(key, addresses);
            }
        }

        return Collections.singletonMap(key, null);
    }

    public void setTree(BTree<String, List<Long>> tree) {
        this.tree = tree;
    }

    public void setStorage(ValueStorage storage) {
        this.storage = storage;
    }
}
