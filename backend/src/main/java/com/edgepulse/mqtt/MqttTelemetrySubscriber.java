package com.edgepulse.mqtt;

import com.edgepulse.config.MqttProperties;
import com.edgepulse.dto.TelemetryMessage;
import com.edgepulse.service.TelemetryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class MqttTelemetrySubscriber implements ApplicationRunner, MqttCallback {
    private static final Logger log = LoggerFactory.getLogger(MqttTelemetrySubscriber.class);

    private final MqttProperties properties;
    private final ObjectMapper objectMapper;
    private final TelemetryService telemetryService;
    private MqttClient client;

    public MqttTelemetrySubscriber(MqttProperties properties, ObjectMapper objectMapper, TelemetryService telemetryService) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.telemetryService = telemetryService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!properties.isEnabled()) {
            log.info("MQTT subscriber is disabled.");
            return;
        }
        client = new MqttClient(properties.getBrokerUrl(), properties.getClientId(), new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        if (properties.getUsername() != null && !properties.getUsername().isBlank()) {
            options.setUserName(properties.getUsername());
            options.setPassword(properties.getPassword() == null ? new char[0] : properties.getPassword().toCharArray());
        }
        client.setCallback(this);
        client.connect(options);
        client.subscribe(properties.getTelemetryTopic(), 1);
        log.info("Subscribed to MQTT topic {} on {}", properties.getTelemetryTopic(), properties.getBrokerUrl());
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("MQTT connection lost: {}", cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        String payload = new String(mqttMessage.getPayload());
        TelemetryMessage message = objectMapper.readValue(payload, TelemetryMessage.class);
        telemetryService.ingest(message);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    @PreDestroy
    public void close() throws Exception {
        if (client != null && client.isConnected()) {
            client.disconnect();
        }
    }
}
