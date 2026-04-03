package com.medical.stomp;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.pojo.BindingInfo;
import com.medical.pojo.Data;
import com.medical.pojo.Waveform;
import com.medical.pojo.WaveformParameter;
import com.medical.service.DeviceBindingService;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class StompDataConsumer {

    @Value("${stomp.server.url:ws://10.242.20.251:8080/ws}")
    private String serverUrl;

    @Value("${stomp.topic:/data/sub/34:81:F4:75:20:70}")
    private String topic;

    // 设备MAC地址，用于从绑定接口查询surgeryId
    @Value("${stomp.device-id:}")
    private String deviceId;
    
    // 绑定查询间隔（毫秒）
    @Value("${stomp.binding-check-interval:5000}")
    private long bindingCheckInterval;

    @Autowired
    private DeviceBindingService deviceBindingService;

    @Autowired
    private DataSource dataSource;

    private WebSocketStompClient stompClient;
    private StompSession session;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, CachedBindingInfo> bindingCache = new ConcurrentHashMap<>();
    // Cache for devices with no binding: DeviceID -> LastCheckTimestamp
    private final Map<String, Long> noBindingCache = new ConcurrentHashMap<>();
    
    // 当前绑定的 surgeryId（从绑定接口获取）
    private volatile Long currentSurgeryId = null;
    private volatile long lastBindingCheckTime = 0;
    
    // Buffer for batch writing
    private final LinkedBlockingQueue<Object> buffer = new LinkedBlockingQueue<>(50000);
    
    // Cache for downsampling parameters: DeviceID -> ParameterID -> LastRecord
    private final Map<String, Map<Integer, LastRecord>> lastRecordCache = new ConcurrentHashMap<>();
    
    // Counter for downsampling waveforms: DeviceID -> Counter
    private final Map<String, Long> waveformCounters = new ConcurrentHashMap<>();

    // Statistics for logging
    private final Map<Long, AtomicInteger> waveformStats = new ConcurrentHashMap<>();
    private final Map<Long, AtomicInteger> parameterStats = new ConcurrentHashMap<>();

    private ScheduledExecutorService scheduler;
    private JdbcTemplate jdbcTemplate;
    
    // Debug counter
    private final AtomicInteger debugLogCount = new AtomicInteger(0);

    @PostConstruct
    public void init() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        scheduler = Executors.newScheduledThreadPool(2);
        
        // Start batch writer
        scheduler.scheduleAtFixedRate(this::flushBuffer, 100, 50, TimeUnit.MILLISECONDS);
        
        // Start logging stats (every 3 seconds)
        scheduler.scheduleAtFixedRate(this::logStats, 3, 3, TimeUnit.SECONDS);

        connectStomp();
    }

    private void logStats() {
        Set<Long> surgeryIds = new HashSet<>();
        surgeryIds.addAll(waveformStats.keySet());
        surgeryIds.addAll(parameterStats.keySet());

        for (Long id : surgeryIds) {
            int waveCount = waveformStats.getOrDefault(id, new AtomicInteger(0)).getAndSet(0);
            int paramCount = parameterStats.getOrDefault(id, new AtomicInteger(0)).getAndSet(0);
            
            if (waveCount > 0 || paramCount > 0) {
                log.info("Backend batch write stats: SurgeryID [{}], Waveforms [{}] records, Parameters [{}] records", id, waveCount, paramCount);
            }
        }
        // Cleanup empty entries to prevent memory leaks
        waveformStats.entrySet().removeIf(e -> e.getValue().get() == 0);
        parameterStats.entrySet().removeIf(e -> e.getValue().get() == 0);
    }

    private void connectStomp() {
        WebSocketClient client = new StandardWebSocketClient();
        stompClient = new WebSocketStompClient(client);
        
        // Use a composite converter to support byte[], String, and JSON
        List<MessageConverter> converters = new ArrayList<>();
        
        // Custom converter to force byte[] conversion regardless of content-type
        converters.add(new MessageConverter() {
            @Override
            public Object fromMessage(Message<?> message, Class<?> targetClass) {
                if (targetClass.equals(byte[].class) && message.getPayload() instanceof byte[]) {
                    return message.getPayload();
                }
                return null;
            }

            @Override
            public Message<?> toMessage(Object payload, MessageHeaders headers) {
                if (payload instanceof byte[]) {
                    return new GenericMessage<>((byte[]) payload, headers);
                }
                return null;
            }
        });
        
        converters.add(new StringMessageConverter());
        converters.add(new MappingJackson2MessageConverter());
        stompClient.setMessageConverter(new CompositeMessageConverter(converters));

        StompSessionHandler sessionHandler = new MyStompSessionHandler();
        
        log.info("Connecting to STOMP server: {}", serverUrl);
        stompClient.connectAsync(serverUrl, sessionHandler);
    }

    private class MyStompSessionHandler extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession stompSession, StompHeaders connectedHeaders) {
            session = stompSession;
            log.info("Connected to STOMP server. Session ID: {}", session.getSessionId());
            
            log.info("Subscribing to topic: {}", topic);
            session.subscribe(topic, new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    // Return byte[] to avoid converter issues if content-type is missing or incorrect
                    return byte[].class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    try {
                        // Manually deserialize using ObjectMapper
                        byte[] bytes = (byte[]) payload;
                        
                        // Debug logging for the first 5 messages
                        if (debugLogCount.get() < 5) {
                            String json = new String(bytes, StandardCharsets.UTF_8);
                            log.info("DEBUG - Raw JSON: {}", json);
                            Data data = objectMapper.readValue(bytes, Data.class);
                            log.info("DEBUG - Parsed Data: {}", data);
                            debugLogCount.incrementAndGet();
                            processMessage(data);
                        } else {
                            Data data = objectMapper.readValue(bytes, Data.class);
                            processMessage(data);
                        }
                    } catch (Exception e) {
                        log.error("Error processing message", e);
                    }
                }
            });
        }

        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            log.error("STOMP Error", exception);
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            log.error("STOMP Transport Error", exception);
            // Reconnect logic could be added here
            scheduler.schedule(() -> connectStomp(), 5, TimeUnit.SECONDS);
        }
    }

    private String getDeviceIdFromTopic(String topic) {
        // Assumes topic format /data/sub/{deviceId}
        String[] parts = topic.split("/");
        return parts[parts.length - 1];
    }

    // Counter for unbound device logs (to avoid spam)
    private final AtomicInteger unboundLogCount = new AtomicInteger(0);
    private volatile long lastUnboundLogTime = 0;

    /**
     * 从绑定接口获取当前设备绑定的 surgeryId
     * 定期刷新，避免频繁请求
     */
    private Long getSurgeryIdFromBinding() {
        long now = System.currentTimeMillis();
        
        // 如果缓存有效，直接返回
        if (currentSurgeryId != null && (now - lastBindingCheckTime) < bindingCheckInterval) {
            return currentSurgeryId;
        }
        
        // 需要查询绑定接口
        if (deviceId == null || deviceId.isEmpty()) {
            // 没有配置设备ID，无法查询
            return null;
        }
        
        try {
            BindingInfo bindingInfo = deviceBindingService.getBindingInfo(deviceId);
            lastBindingCheckTime = now;
            
            if (bindingInfo != null && bindingInfo.getSurgeryId() != null) {
                Long newSurgeryId = Long.parseLong(bindingInfo.getSurgeryId());
                if (!newSurgeryId.equals(currentSurgeryId)) {
                    log.info("🔗 设备 [{}] 绑定到 surgeryId [{}]", deviceId, newSurgeryId);
                    currentSurgeryId = newSurgeryId;
                }
                return currentSurgeryId;
            } else {
                if (currentSurgeryId != null) {
                    log.info("🔗 设备 [{}] 解除绑定", deviceId);
                    currentSurgeryId = null;
                }
                return null;
            }
        } catch (Exception e) {
            log.warn("查询设备绑定失败: {}", e.getMessage());
            return currentSurgeryId; // 返回上次的缓存值
        }
    }

    private void processMessage(Data data) {
        Long surgeryId = null;
        String cacheKey;

        // 优先使用数据中的 surgeryId (from /data/sub/all)
        if (data.getSurgeryId() != null && !data.getSurgeryId().isEmpty()) {
            try {
                surgeryId = Long.parseLong(data.getSurgeryId());
                cacheKey = "surgery_" + surgeryId;
                // Log when receiving data for a bound surgery
                if (debugLogCount.get() < 10) {
                    log.info("✅ 从数据中获取 surgeryId [{}]: hr={}, bo={}, temp={}", 
                        surgeryId, data.getHr(), data.getBo(), data.getTemp());
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid surgeryId format: {}", data.getSurgeryId());
                return;
            }
        } else {
            // 数据中没有 surgeryId，尝试从绑定接口获取
            surgeryId = getSurgeryIdFromBinding();
            
            if (surgeryId != null) {
                cacheKey = "surgery_" + surgeryId;
                // 每30秒打印一次日志
                long now = System.currentTimeMillis();
                if (now - lastUnboundLogTime > 30000) {
                    lastUnboundLogTime = now;
                    log.info("✅ 从绑定接口获取 surgeryId [{}]: hr={}, bo={}, temp={}", 
                        surgeryId, data.getHr(), data.getBo(), data.getTemp());
                }
            } else {
                // 设备未绑定，跳过
                long now = System.currentTimeMillis();
                if (now - lastUnboundLogTime > 30000) {
                    lastUnboundLogTime = now;
                    int count = unboundLogCount.getAndSet(0);
                    log.info("⏭️ 跳过 {} 条数据 (设备 [{}] 未绑定到任何手术)", count + 1, deviceId);
                } else {
                    unboundLogCount.incrementAndGet();
                }
                return;
            }
        }

        if (surgeryId == null) {
            return;
        }

        Instant time = Instant.ofEpochMilli(data.getTimestamp());

        // Process Waveforms (only if ecg/boWave/respWave are present)
        if (data.getEcg() != 0) {
            // ECG (250Hz) - Parameter ID 1
            buffer.offer(new Waveform(time, surgeryId, 1, (int) data.getEcg()));

            // SpO2 Wave (50Hz) - Parameter ID 5
            // Resp Wave (50Hz) - Parameter ID 6
            // Downsample 250Hz -> 50Hz (1 in 5)
            long count = waveformCounters.merge(cacheKey, 1L, Long::sum);
            if (count % 5 == 0) {
                buffer.offer(new Waveform(time, surgeryId, 5, (int) data.getBoWave()));
                buffer.offer(new Waveform(time, surgeryId, 6, (int) data.getRespWave()));
            }
        }

        // Process Parameters (low-frequency data from /data/sub/all)
        if (data.getHr() > 0) {
            processParameter(cacheKey, surgeryId, time, 2, (float) data.getHr(), 1000); // HR 1Hz
        }
        if (data.getBp() > 0) {
            processParameter(cacheKey, surgeryId, time, 3, data.getBp(), 500); // BP 2Hz
        }
        if (data.getBo() > 0) {
            processParameter(cacheKey, surgeryId, time, 4, (float) data.getBo(), 1000); // SpO2 1Hz
        }
        if (data.getTemp() > 0) {
            processParameter(cacheKey, surgeryId, time, 7, data.getTemp(), 2000); // Temp 0.5Hz
        }
        if (data.getResp() > 0) {
            processParameter(cacheKey, surgeryId, time, 8, (float) data.getResp(), 1000); // Resp 1Hz
        }
    }

    private void processParameter(String cacheKey, long surgeryId, Instant time, int paramId, float value, long minIntervalMs) {
        Map<Integer, LastRecord> deviceRecords = lastRecordCache.computeIfAbsent(cacheKey, k -> new ConcurrentHashMap<>());
        LastRecord last = deviceRecords.get(paramId);

        boolean shouldSave = false;
        if (last == null) {
            shouldSave = true;
        } else {
            long timeDiff = time.toEpochMilli() - last.timestamp;
            boolean valueChanged = Math.abs(value - last.value) > 0.001; // Epsilon for float
            
            if (timeDiff >= minIntervalMs || valueChanged) {
                shouldSave = true;
            }
        }

        if (shouldSave) {
            buffer.offer(new WaveformParameter(time, surgeryId, paramId, value));
            deviceRecords.put(paramId, new LastRecord(value, time.toEpochMilli()));
        }
    }

    private BindingInfo getBindingInfo(String deviceId) {
        CachedBindingInfo cached = bindingCache.get(deviceId);
        if (cached != null && System.currentTimeMillis() - cached.timestamp < 5000) {
            return cached.info;
        }

        // Check if we recently checked and found no binding (cache for 5 seconds)
        Long lastCheck = noBindingCache.get(deviceId);
        if (lastCheck != null && System.currentTimeMillis() - lastCheck < 5000) {
            return null;
        }

        BindingInfo info = deviceBindingService.getBindingInfo(deviceId);
        if (info != null) {
            bindingCache.put(deviceId, new CachedBindingInfo(info, System.currentTimeMillis()));
            noBindingCache.remove(deviceId);
        } else {
            noBindingCache.put(deviceId, System.currentTimeMillis());
        }
        return info;
    }

    private void flushBuffer() {
        if (buffer.isEmpty()) return;

        List<Object> batch = new ArrayList<>();
        buffer.drainTo(batch, 1000); 

        if (batch.isEmpty()) return;

        List<Waveform> waveforms = new ArrayList<>();
        List<WaveformParameter> parameters = new ArrayList<>();

        for (Object obj : batch) {
            if (obj instanceof Waveform) {
                waveforms.add((Waveform) obj);
            } else if (obj instanceof WaveformParameter) {
                parameters.add((WaveformParameter) obj);
            }
        }

        if (!waveforms.isEmpty()) {
            batchInsertWaveforms(waveforms);
        }
        if (!parameters.isEmpty()) {
            batchInsertParameters(parameters);
        }
    }

    private void batchInsertWaveforms(List<Waveform> list) {
        // Update stats
        for (Waveform w : list) {
            waveformStats.computeIfAbsent(w.getTreatmentInformationId(), k -> new AtomicInteger(0)).incrementAndGet();
        }

        // 使用 ON CONFLICT DO NOTHING 避免主键冲突导致整批失败
        String sql = "INSERT INTO public.waveform (time, treatment_information_id, parameter_id, amplitude) " +
                     "VALUES (?, ?, ?, ?) ON CONFLICT DO NOTHING";
        try {
            jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Waveform w = list.get(i);
                    // 使用 TIMESTAMPTZ 类型
                    ps.setObject(1, java.time.OffsetDateTime.ofInstant(w.getTime(), java.time.ZoneId.systemDefault()));
                    ps.setLong(2, w.getTreatmentInformationId());
                    ps.setInt(3, w.getParameterId());
                    ps.setInt(4, w.getAmplitude());
                }

                @Override
                public int getBatchSize() {
                    return list.size();
                }
            });
        } catch (Exception e) {
            log.error("Error batch inserting waveforms: {}", e.getMessage());
        }
    }

    private void batchInsertParameters(List<WaveformParameter> list) {
        // Update stats
        for (WaveformParameter p : list) {
            parameterStats.computeIfAbsent(p.getTreatmentInformationId(), k -> new AtomicInteger(0)).incrementAndGet();
        }

        // 使用 ON CONFLICT DO NOTHING 避免主键冲突导致整批失败
        String sql = "INSERT INTO public.waveform_parameter (time, treatment_information_id, parameter_id, value) " +
                     "VALUES (?, ?, ?, ?) ON CONFLICT DO NOTHING";
        try {
            jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    WaveformParameter p = list.get(i);
                    // 使用 TIMESTAMPTZ 类型
                    ps.setObject(1, java.time.OffsetDateTime.ofInstant(p.getTime(), java.time.ZoneId.systemDefault()));
                    ps.setLong(2, p.getTreatmentInformationId());
                    ps.setInt(3, p.getParameterId());
                    ps.setFloat(4, p.getValue());
                }

                @Override
                public int getBatchSize() {
                    return list.size();
                }
            });
        } catch (Exception e) {
            log.error("Error batch inserting parameters: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void cleanup() {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    private static class LastRecord {
        float value;
        long timestamp;

        public LastRecord(float value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }
    }

    private static class CachedBindingInfo {
        BindingInfo info;
        long timestamp;

        public CachedBindingInfo(BindingInfo info, long timestamp) {
            this.info = info;
            this.timestamp = timestamp;
        }
    }
}
