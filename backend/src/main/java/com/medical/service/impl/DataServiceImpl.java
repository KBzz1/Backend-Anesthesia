package com.medical.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.pojo.Data;
import com.medical.service.DeviceBindingService;
import com.medical.service.DataService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DataServiceImpl implements DataService {

    private static final Logger log = LoggerFactory.getLogger(DataServiceImpl.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SimpMessagingTemplate messagingTemplate;
    private final DeviceBindingService deviceBindingService;
    private final Map<String, Long> lowFreqLastSentTime = new ConcurrentHashMap<>();
    private static final long LOW_FREQ_INTERVAL_MS = 1000L;

    public DataServiceImpl(SimpMessagingTemplate messagingTemplate, DeviceBindingService deviceBindingService) {
        this.messagingTemplate = messagingTemplate;
        this.deviceBindingService = deviceBindingService;
    }

    @Override
    public void publish(String deviceId, Data data) {
        try {
            if (data.getHr() <= 0 || data.getTemp() <= 25 || data.getTemp() >= 45) {
                log.warn("Ignoring abnormal data from device {}: {}", deviceId, data);
                return;
            }
            String json = objectMapper.writeValueAsString(data);
            String topic = String.format("device/%s", deviceId);
            String surgeryId = deviceBindingService.getDevice(deviceId);
            if (surgeryId != null && !surgeryId.isBlank()) {
                data.setSurgeryId(surgeryId);
            }
            messagingTemplate.convertAndSend(String.format("/data/sub/%s", deviceId), data);
            pushLowFrequencyAggregate(deviceId, data);
            log.info("Ready to publish -> {} : {}", topic, json);
        } catch (Exception e) {
            log.error("Failed to process data from device {}", deviceId, e);
        }
    }

    private void pushLowFrequencyAggregate(String deviceId, Data data) {
        long now = System.currentTimeMillis();
        long lastSent = lowFreqLastSentTime.getOrDefault(deviceId, 0L);
        if (now - lastSent < LOW_FREQ_INTERVAL_MS) {
            return;
        }
        lowFreqLastSentTime.put(deviceId, now);

        Map<String, Object> lowFreqPayload = new java.util.LinkedHashMap<>();
        lowFreqPayload.put("surgeryId", data.getSurgeryId() == null ? "" : data.getSurgeryId());
        lowFreqPayload.put("resp", data.getResp());
        lowFreqPayload.put("bo", data.getBo());
        lowFreqPayload.put("hr", data.getHr());
        lowFreqPayload.put("temp", data.getTemp());
        lowFreqPayload.put("timestamp", data.getTimestamp());
        messagingTemplate.convertAndSend("/data/sub/all", lowFreqPayload);
    }
}

