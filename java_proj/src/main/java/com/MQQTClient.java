package com;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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


public class MQQTClient implements Queryable{
    private static final Logger logger = Logger.getLogger(MQQTClient.class.getName());
    private static final String REQUEST_TOPIC= "twosides/requests";
    private static final String RESPONSE_TOPIC= "twosides/responses";
    private IMqttClient client;
    private Gson gson=new Gson();
    private Map<String, String> responseCache = new HashMap<>();
    
    /**
     * Constructor for the MQQTClient class
     * 
     * @throws MqttException
     * @throws InterruptedException
     */
    MQQTClient(String broker) throws MqttException, InterruptedException {
        logger.log(Level.INFO, "Broker: {0}", broker);

        String clientId  = UUID.randomUUID().toString();
        logger.log(Level.INFO, "My ID: {0}", clientId);

        client = new MqttClient(broker, clientId);
        logger.log(Level.INFO, "Client: {0}", client);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        logger.log(Level.INFO, "Connect options: {0}", options);

        client.connect(options);
        if (!client.isConnected()) {
            logger.log(Level.SEVERE, "NOT Connected!");
            client.close();
            return;
        }
        logger.log(Level.INFO, "Connected!");

        client.subscribe(RESPONSE_TOPIC, this::handleResponse);
    }

    /**
     * Method to handle the response
     * 
     * @param topic channel to listen to
     * @param message message received
     */
    private void handleResponse(String topic, MqttMessage message) {
        try {
            String payload = new String(message.getPayload());
            logger.log(Level.INFO, "Message received: {0}", payload);
            JsonObject rjson = JsonParser.parseString(payload).getAsJsonObject();
            String requestId = rjson.get("requestId").getAsString();
            String responseData = rjson.get("data").toString();

            synchronized (responseCache) {
                responseCache.put(requestId, responseData);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error handling response", e);
        }
    }

    /**
     * Method to query the drug
     * 
     * @param drugName name of the drug
     * @param like boolean value
     * @return list of strings
     * @throws MqttException
     * @throws InterruptedException
     */
    public List<String> queryDrug(String drugName, boolean like) {
        try {
            String requestId = UUID.randomUUID().toString();
            JsonObject request = new JsonObject();
            request.addProperty("requestId", requestId);
            request.addProperty("action", "queryDrug");
            JsonObject params = new JsonObject();
            params.addProperty("drugName", drugName);
            params.addProperty("like", like);
            request.add("params", params);

            MqttMessage requestMessage=new MqttMessage(gson.toJson(request).getBytes());
            requestMessage.setQos(1);
            client.publish(REQUEST_TOPIC, requestMessage);
            String response=waitForResponse(requestId);
            return gson.fromJson(response, List.class);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupt status
            throw new RuntimeException("Thread was interrupted during MQTT query", e);
        } catch (MqttException e) {
            throw new RuntimeException("MQTT exception occurred during query", e);
        }
    }

    /**
     * Method to query the twosides
     * 
     * @param drug1Name name of the first drug
     * @param drug2Name name of the second drug
     * @param filtered boolean value
     * @return TwosidesCol object
     * @throws MqttException
     * @throws InterruptedException
     */
    public TwosidesCol queryTwosides(String drug1Name, String drug2Name, boolean filtered) {
        try {
            String requestId = UUID.randomUUID().toString();
            JsonObject request = new JsonObject();
            request.addProperty("requestId", requestId);
            request.addProperty("action", "queryTwosides");
            JsonObject params = new JsonObject();
            params.addProperty("drug1Name", drug1Name);
            params.addProperty("drug2Name", drug2Name);
            params.addProperty("filtered", filtered);
            request.add("params", params);
        
            MqttMessage requestMessage = new MqttMessage(gson.toJson(request).getBytes());
            requestMessage.setQos(1);
            client.publish(REQUEST_TOPIC, requestMessage);
            String response = waitForResponse(requestId);
            return gson.fromJson(response, TwosidesCol.class);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupt status
            throw new RuntimeException("Thread was interrupted during MQTT query", e);
        } catch (MqttException e) {
            throw new RuntimeException("MQTT exception occurred during query", e);
        }
    }

    /**
     * Method to wait for the response
     * 
     * @param requestId id of the request
     * @return response
     * @throws InterruptedException
     */
    private String waitForResponse(String requestId) throws InterruptedException {
        int timeout=300000; //5 minutes
        int interval=100;
        int elapsed=0;

        while (elapsed<timeout) {
            synchronized (responseCache) {
                if (responseCache.containsKey(requestId)) {
                    return responseCache.remove(requestId);
                }
            }
            Thread.sleep(interval);
            elapsed+=interval;
        }
        throw new InterruptedException("Timeout waiting for response");
    }
}
