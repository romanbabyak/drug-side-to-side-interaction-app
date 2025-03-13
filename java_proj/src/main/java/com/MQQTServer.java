package com;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MQQTServer {
    private static final Logger logger = Logger.getLogger(MQQTServer.class.getName());
    private static final String REQUEST_TOPIC = "twosides/requests";
    private static final String RESPONSE_TOPIC = "twosides/responses";
    private IMqttClient client;
    private DatabaseQuery dbQuery;
    private Gson gson;

    /**
     * Constructor for the MQQTServer class
     * 
     * @throws MqttException
     */
    public MQQTServer(String broker, String url, String user, String password) throws MqttException {
        this.dbQuery = new DatabaseQuery();
        this.dbQuery.connectInitially(url, user, password);
        this.gson = new Gson();

        logger.log(Level.INFO, "Broker: {0}", broker);

        String clientId = UUID.randomUUID().toString();
        logger.log(Level.INFO, "Client ID: {0}", clientId);

        client = new MqttClient(broker, clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);

        client.connect(options);
        if (!client.isConnected()) {
            logger.log(Level.SEVERE, "Failed to connect to the broker");
            client.close();
            return;
        }
        logger.log(Level.INFO, "Connected to the broker");

        client.subscribe(REQUEST_TOPIC, this::handleMessage);
    }

    /**
     * Handles the incoming message
     * 
     * @param topic
     * @param message
     */
    private void handleMessage(String topic, MqttMessage message) {
        try {
            String payload = new String(message.getPayload());
            logger.log(Level.INFO, "Received message: {0}", payload);

            JsonObject json = JsonParser.parseString(payload).getAsJsonObject();
            String method = json.get("action").getAsString();
            JsonObject params = json.getAsJsonObject("params");

            logger.log(Level.INFO, "Action: {0}", method);
            logger.log(Level.INFO, "Params: {0}", params);

            String response;

            switch (method) {
                case "queryTwosides":
                    String drug1Name = params.get("drug1Name").getAsString();
                    String drug2Name = params.get("drug2Name").getAsString();
                    boolean filtered = params.get("filtered").getAsBoolean();

                    response = gson.toJson(dbQuery.queryTwosides(drug1Name, drug2Name, filtered));
                    break;

                case "queryDrug":
                    String drugName = params.get("drugName").getAsString();
                    boolean like = params.get("like").getAsBoolean();

                    response = gson.toJson(dbQuery.queryDrug(drugName, like));
                    break;

                default:
                    response = gson.toJson("Unknown method: " + method);
            }

            publishResponse(response, json.get("requestId").getAsString());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred while handling the message", e);
        }
    }

    /**
     * Publishes the response
     * 
     * @param response
     * @param requestID
     * @throws MqttException
     */
    private void publishResponse(String response, String requestID) throws MqttException {
        try {
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("requestId", requestID);
            responseJson.add("data", JsonParser.parseString(response)); 
            MqttMessage responseMessage = new MqttMessage(responseJson.toString().getBytes());
            responseMessage.setQos(1);
            client.publish(RESPONSE_TOPIC, responseMessage);
            logger.log(Level.INFO, "Published response: {0}", responseMessage);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to publish response", e);
        }
    }
}
