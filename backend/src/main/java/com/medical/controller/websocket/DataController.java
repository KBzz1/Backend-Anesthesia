package com.medical.controller.websocket;

import com.medical.pojo.Data;
import com.medical.service.DataService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Controller("stompDataController")
public class DataController {

    private final DataService dataService;
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, AtomicInteger> deviceMsgCount = new ConcurrentHashMap<>();

    public DataController(DataService dataService, SimpMessagingTemplate messagingTemplate) {
        this.dataService = dataService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/pub/{deviceId}")
    public void receiveData(@DestinationVariable String deviceId, @Payload Data data) {
        dataService.publish(deviceId, data);
        deviceMsgCount.computeIfAbsent(deviceId, ignored -> new AtomicInteger()).incrementAndGet();
    }

    @Scheduled(fixedDelay = 5000L)
    void flushAckCounts() {
        long now = System.currentTimeMillis();
        deviceMsgCount.forEach((deviceId, counter) -> {
            int count = counter.getAndSet(0);
            if (count > 0) {
                String ackJson = "{\"count\":" + count + ",\"t\":" + now + "}";
                messagingTemplate.convertAndSend("/data/pub/response", ackJson);
            }
        });
    }
}
