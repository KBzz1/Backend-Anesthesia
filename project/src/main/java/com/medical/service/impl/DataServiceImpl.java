package com.medical.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.pojo.Data;
import com.medical.service.DataService;

@Service
public class DataServiceImpl implements DataService {

    private static final Logger log = LoggerFactory.getLogger(DataServiceImpl.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void publish(String deviceId, Data data) {
        try {
            if (data.getHr() <= 0 || data.getTemp() <= 25 || data.getTemp() >= 45) {
                log.warn("Ignoring abnormal data from device {}: {}", deviceId, data);
                return;
            }
            String json = objectMapper.writeValueAsString(data);
            String topic = String.format("device/%s", deviceId);
            // Publish hook can be plugged in here (e.g. MQTT broker integration)
            log.info("Ready to publish -> {} : {}", topic, json);
        } catch (Exception e) {
            log.error("Failed to process data from device {}", deviceId, e);
        }
    }
}

