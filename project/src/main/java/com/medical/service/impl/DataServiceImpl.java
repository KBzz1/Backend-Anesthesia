package com.medical.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.pojo.Data;
import com.medical.service.DataService;
import com.medical.utils.MqttManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataServiceImpl implements DataService {

//    @Autowired
//    private MqttManager mqttPublisher;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void publish(String deviceId, Data data) {
        try {
            // 简单校验
            if (data.getHr() <= 0 || data.getTemp() <= 25 || data.getTemp() >= 45) {
                System.err.println("数据异常: " + data);
                return;
            }
            // 转成 JSON
            String json = objectMapper.writeValueAsString(data);
            // 发布到 MQTT
            String topic = String.format("device/%s", deviceId);
//            mqttPublisher.publish(topic, json);
            // 可扩展：保存数据库或异步分析
            System.out.println("发布成功 -> " + topic + " : " + json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理从 MQTT 订阅接收到的消息
     */
    public void subscribe(String topic, String payload) {
        try {
            // 解析 JSON 数据
            Data data = objectMapper.readValue(payload, Data.class);

            // 从 topic 中提取 deviceId（如 device/101/vitals）
            String deviceId = extractDeviceId(topic);

            System.out.printf("📡 Received data from device %s: HR=%d, Temp=%.2f°C%n",
                    deviceId, data.getHr(), data.getTemp());

            // 这里可进行入库、分析或告警等业务逻辑
            // example: saveToDatabase(deviceId, data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String extractDeviceId(String topic) {
        // topic 格式: device/{deviceId}/vitals
        String[] parts = topic.split("/");
        if (parts.length >= 2) {
            return parts[1];
        }
        return "unknown";
    }




}
