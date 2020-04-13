package com.devtritus.deusbase.node.role;

import com.devtritus.deusbase.api.Command;
import com.devtritus.deusbase.api.NodeClient;
import com.devtritus.deusbase.api.ResponseBody;
import com.devtritus.deusbase.node.env.NodeEnvironment;
import com.devtritus.deusbase.node.server.SlaveApi;
import org.apache.http.conn.HttpHostConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SlaveNode implements SlaveApi {
    private final static Logger logger = LoggerFactory.getLogger(SlaveNode.class);

    private NodeEnvironment env;

    public SlaveNode(NodeEnvironment env) {
        this.env = env;
    }

    public void init(String slaveAddress, String masterAddress) {
        handshake(slaveAddress, masterAddress);
    }

    @Override
    public void receiveLog(Map<String, byte[]> logPart) {

    }

    private void handshake(String slaveAddress, String masterAddress) {
        NodeClient client = new NodeClient("http://" + masterAddress);
        String slaveUuid = env.getPropertyOrThrowException("uuid");

        ResponseBody responseBody;
        try {
            responseBody = client.request(Command.HANDSHAKE, slaveAddress, slaveUuid);
        } catch (HttpHostConnectException e) {
            logger.error("Master node through address {} is unavailable. Only READ mode is permitted", masterAddress);
            return;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String actualMasterUuid = responseBody.getData().get("result").get(0);
        String writtenMasterUuid = env.getProperty("masterUuid");
        if(writtenMasterUuid == null) { //first connection
            env.setProperty("masterUuid", actualMasterUuid);
        } else if(!actualMasterUuid.equals(writtenMasterUuid)) {
            throw new IllegalStateException(String.format("Slave isn't owned to master located at %s", masterAddress));
        }
    }
}
