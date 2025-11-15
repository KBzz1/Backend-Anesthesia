package com.medical.utils;

import com.medical.service.DataService;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;

// MQTT 发布组件（长连接）
@Component
public class MqttManager {

    private IMqttClient client;


    // 配置属性注入
    // 发布配置
    @Value("${mqtt.broker}")
    private String mqttBroker;
    @Value("${mqtt.clientId}")
    private String clientId;
    @Value("${mqtt.username}")
    private String username;
    @Value("${mqtt.password}")
    private String password;
    // 订阅配置
    @Value("${mqtt.topic:device/+/vitals}")
    private String topic;
//    @Autowired
//    private DataService dataService;


    @PostConstruct
    public void init() throws MqttException {
        client = new MqttClient(mqttBroker, clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(false);
        options.setKeepAliveInterval(30);

        // 加入认证信息
        options.setUserName(username);
        options.setPassword(password.toCharArray());

        client.connect(options);
        System.out.println("✔ MQTT connected to " + mqttBroker + " as " + clientId);
        // 订阅主题
        client.subscribe(topic, this::handleMessage);
        System.out.println("Subscribed to topic: " + topic);
    }

    public void publish(String topic, String payload) {
        try {
            if (!client.isConnected()) {
                client.reconnect();
            }
            MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
            message.setQos(0);
            client.publish(topic, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleMessage(String topic, MqttMessage message) {
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        System.out.printf("[MQTT] Received topic: %s, payload: %s%n", topic, payload);

//        // 调用业务类处理数据
//        dataService.subscribe(topic, payload);
    }

    @PreDestroy
    public void cleanup() throws MqttException {
        if (client != null && client.isConnected()) {
            client.disconnect();
        }
    }
}

