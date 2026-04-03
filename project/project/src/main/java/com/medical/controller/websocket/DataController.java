package com.medical.controller.websocket;

import com.medical.pojo.Data;
import com.medical.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

// 监测数据管理，websocket协议
@Controller
public class DataController {

    @Autowired
    DataService dataService;
    private final SimpMessagingTemplate messagingTemplate;
    // 保存每个设备的计数
    private final Map<String, AtomicInteger> deviceMsgCount = new ConcurrentHashMap<>();

    public DataController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        // 定时任务，每秒批量响应一次
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000); // 每5秒响应一次
                    long now = System.currentTimeMillis();

                    deviceMsgCount.forEach((deviceId, counter) -> {
                        int count = counter.getAndSet(0); // 重置计数器
                        if (count > 0) {
                            // 轻量响应：已处理消息数 + 确认时间戳
                            String ackJson = "{\"count\":" + count + ",\"t\":" + now + "}";
                            messagingTemplate.convertAndSend("/data/pub/response", ackJson);
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }


    /**
     * 接收设备数据
     * 客户端发送到 /data/pub/{deviceId}
     */
    @MessageMapping("/pub/{deviceId}")
    public void receiveData(@DestinationVariable String deviceId,
                            @Payload Data data) {
        // 转发到 MQTT
        dataService.publish(deviceId, data);
        // 计数器累加
        deviceMsgCount.computeIfAbsent(deviceId, k -> new AtomicInteger()).incrementAndGet();

    }


}
