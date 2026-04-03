package com.medical.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.service.DeviceBindingService;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// MQTT 发布组件（长连接）
@Component
public class MqttManager {
    private static final Logger log = LoggerFactory.getLogger(MqttManager.class);
    private static final long INITIAL_RETRY_SECONDS = 2L;
    private static final long MAX_RETRY_SECONDS = 30L;

    private IMqttClient client;

    @Autowired
    private DeviceBindingService deviceBindingService;

    // 配置属性注入
    @Value("${mqtt.broker}")
    private String mqttBroker;
    @Value("${mqtt.clientId}")
    private String clientId;
    @Value("${mqtt.username}")
    private String username;
    @Value("${mqtt.password}")
    private String password;
    // topic集合，避免重复订阅mqtt
    private final Set<String> subscribedTopics = ConcurrentHashMap.newKeySet();
    private final ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor();

    // 长连接管道
    private final SimpMessagingTemplate messagingTemplate;
    public MqttManager(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // 监听客户端的订阅地址变化，获取设备id
    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String destination = sha.getDestination();
        // 只监听 /data/sub/{deviceId} 这种目的地
        if (destination != null && destination.startsWith("/data/sub/")) {
            String deviceId = destination.substring("/data/sub/".length());
            log.info("[STOMP] client subscribed device {}", deviceId);
            // 调用 MqttManager 动态订阅 MQTT 主题
            try {
                String topic = "device/" + deviceId;
                boolean isNewTopic = subscribedTopics.add(topic);
                if (isNewTopic && client != null && client.isConnected()) {
                    client.subscribe(topic, this::handleMessage);
                }
                log.info("MQTT topic tracked: {}", topic);
            } catch (Exception e) {
                log.warn("Failed to subscribe MQTT topic for device {}", deviceId, e);
            }
        }
    }

    // 初始化
    @PostConstruct
    public void init() throws MqttException {
        client = new MqttClient(mqttBroker, clientId);
        scheduleReconnect(0L);
    }

    private void scheduleReconnect(long delaySeconds) {
        reconnectExecutor.schedule(() -> {
            try {
                if (client == null || client.isConnected()) {
                    return;
                }
                MqttConnectOptions options = new MqttConnectOptions();
                options.setAutomaticReconnect(true);
                options.setCleanSession(false);
                options.setKeepAliveInterval(30);
                options.setUserName(username);
                options.setPassword(password.toCharArray());
                client.connect(options);
                log.info("MQTT connected to {} as {}", mqttBroker, clientId);
                resubscribeTopics();
            } catch (MqttException e) {
                long nextDelay = delaySeconds == 0L ? INITIAL_RETRY_SECONDS : Math.min(delaySeconds * 2, MAX_RETRY_SECONDS);
                log.warn("MQTT connect failed, retrying in {}s: {}", nextDelay, e.getMessage());
                scheduleReconnect(nextDelay);
            }
        }, delaySeconds, TimeUnit.SECONDS);
    }

    private void resubscribeTopics() {
        for (String topic : subscribedTopics) {
            try {
                client.subscribe(topic, this::handleMessage);
            } catch (MqttException e) {
                log.warn("Failed to resubscribe MQTT topic {}", topic, e);
            }
        }
    }

    // 发布消息
    public void publish(String topic, String payload) {
        try {
            if (client != null && client.isConnected()) {
                try {
                    MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
                    message.setQos(0);
                    client.publish(topic, message);
                } catch (MqttException e) {
                    log.warn("MQTT publish failed: {}", e.getMessage());
                }
            } else {
                log.warn("MQTT is not connected, dropping message for topic {}", topic);
            }
        } catch (Exception e) {
            log.warn("Unexpected MQTT publish error", e);
        }
    }

    // 记录各device最后低频推送时间，限流低频推送
    private final Map<String, Long> lowFreqLastSentTime = new ConcurrentHashMap<>();
    // 低频推送间隔（1秒）
    private static final long LOW_FREQ_INTERVAL_MS = 1000L;
    private final ObjectMapper mapper = new ObjectMapper();
    // 单线程异步执行低频推送任务，保证顺序性，避免并发冲突
    private final ExecutorService lowFreqExecutor = Executors.newSingleThreadExecutor();
    // 缓存低频数据（包含需要发送的字段）
    private final Map<String, LowFreqData> lowFreqCache = new ConcurrentHashMap<>();
    // 订阅消息
    private void handleMessage(String topic, MqttMessage message) {
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        String deviceId = extractDeviceId(topic); // 从主题中解析设备ID
//        System.out.printf("[MQTT] Received topic: %s, deviceId: %s, payload: %s%n", topic, deviceId, payload);
        try {
            JsonNode root = mapper.readTree(payload);
            // 高频数据直接推送
            messagingTemplate.convertAndSend("/data/sub/" + deviceId, payload);
            // 推送到汇总通道（大屏）,异步推送低频数据
            LowFreqData lowFreqData = extractLowFreqData(root);
            // 这里缓存最新的低频数据
            lowFreqCache.put(deviceId, lowFreqData);
            // 异步推送逻辑
            lowFreqExecutor.submit(() -> {
                long now = System.currentTimeMillis();
                long lastSent = lowFreqLastSentTime.getOrDefault(deviceId, 0L);
                if (now - lastSent >= LOW_FREQ_INTERVAL_MS) {
                    lowFreqLastSentTime.put(deviceId, now);
                    // 从缓存中获取低频数据
                    LowFreqData dataToSend = lowFreqCache.get(deviceId);
                    if (dataToSend == null) return; // 数据不存在，不推送
                    // 获取surgeryId，建议这个操作开销不大，如有开销请做缓存优化
                    String surgeryId = deviceBindingService.getDevice(deviceId);
                    if (surgeryId == null) surgeryId = "";
                    try {
                        // 构造低频数据Json并推送
                        String lowFreqJson = mapper.createObjectNode()
                                .put("resp", dataToSend.resp)
                                .put("bo", dataToSend.bo)
                                .put("hr", dataToSend.hr)
                                .put("temp", dataToSend.temp)
                                .put("timestamp", dataToSend.timestamp)
                                .put("surgeryId", surgeryId)
                                .toString();
                        messagingTemplate.convertAndSend("/data/sub/all", lowFreqJson);
                    } catch (Exception e) {
                        log.warn("Low frequency push failed: {}", e.getMessage());
                    }
                }
            });
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse or push MQTT message: {}", e.getMessage());
        }
    }

    private LowFreqData extractLowFreqData(JsonNode root) {
        int resp = root.has("resp") ? root.get("resp").asInt() : 0;
        int bo = root.has("bo") ? root.get("bo").asInt() : 0;
        int hr = root.has("hr") ? root.get("hr").asInt() : 0;
        double temp = root.has("temp") ? root.get("temp").asDouble() : 0.0;
        long timestamp = root.has("timestamp") ? root.get("timestamp").asLong() : System.currentTimeMillis();

        return new LowFreqData(resp, bo, hr, temp, timestamp);
    }

    // 低频数据容器类
        record LowFreqData(int resp, int bo, int hr, double temp, long timestamp) {
    }

    // 关闭线程池（程序停止时调用）
    public void shutdown() {
        lowFreqExecutor.shutdown();
        try {
            if (!lowFreqExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                lowFreqExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            lowFreqExecutor.shutdownNow();
        }
    }

    private String extractDeviceId(String topic) {
        // 例：device/E2:7A:4C:19:B3:65
        try {
            String[] parts = topic.split("/");
            if (parts.length >= 2 && "device".equals(parts[0])) {
                return parts[1];
            }
        } catch (Exception ignore) { }
        return "unknown";
    }


    @PreDestroy
    public void cleanup() throws MqttException {
        reconnectExecutor.shutdownNow();
        shutdown();
        if (client != null && client.isConnected()) {
            client.disconnect();
            client.close();
        }
    }
}

