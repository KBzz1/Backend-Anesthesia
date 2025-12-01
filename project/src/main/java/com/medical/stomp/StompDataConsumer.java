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

    @Value("${stomp.server.url:ws://10.242.98.103:8080/ws}")
    private String serverUrl;

    @Value("${stomp.topic:/data/sub/34:81:F4:75:20:70}")
    private String topic;

    @Autowired
    private DeviceBindingService deviceBindingService;

    @Autowired
    private DataSource dataSource;

    private WebSocketStompClient stompClient;
    private StompSession session;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, BindingInfo> bindingCache = new ConcurrentHashMap<>();
    // Cache for devices with no binding: DeviceID -> LastCheckTimestamp
    private final Map<String, Long> noBindingCache = new ConcurrentHashMap<>();
    
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
                            
                            String deviceId = getDeviceIdFromTopic(topic);
                            processMessage(deviceId, data);
                        } else {
                            Data data = objectMapper.readValue(bytes, Data.class);
                            String deviceId = getDeviceIdFromTopic(topic);
                            processMessage(deviceId, data);
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

    private void processMessage(String deviceId, Data data) {
        // 1. Get Binding Info
        BindingInfo binding = getBindingInfo(deviceId);
        if (binding == null) {
            return; 
        }

        long surgeryId = binding.getSurgeryId();
        Instant time = Instant.ofEpochMilli(data.getTimestamp());

        // 2. Process Waveforms
        // ECG (250Hz) - Parameter ID 1
        buffer.offer(new Waveform(time, surgeryId, 1, (int) data.getEcg()));

        // SpO2 Wave (50Hz) - Parameter ID 5
        // Resp Wave (50Hz) - Parameter ID 6
        // Downsample 250Hz -> 50Hz (1 in 5)
        long count = waveformCounters.merge(deviceId, 1L, Long::sum);
        if (count % 5 == 0) {
             buffer.offer(new Waveform(time, surgeryId, 5, (int) data.getBoWave()));
             buffer.offer(new Waveform(time, surgeryId, 6, (int) data.getRespWave()));
        }

        // 3. Process Parameters
        processParameter(deviceId, surgeryId, time, 2, (float) data.getHr(), 1000); // HR 1Hz
        processParameter(deviceId, surgeryId, time, 3, data.getBp(), 500); // BP 2Hz
        processParameter(deviceId, surgeryId, time, 4, (float) data.getBo(), 1000); // SpO2 1Hz
        processParameter(deviceId, surgeryId, time, 7, data.getTemp(), 2000); // Temp 1Hz (or slower)
        processParameter(deviceId, surgeryId, time, 8, (float) data.getResp(), 1000); // Resp 1Hz
    }

    private void processParameter(String deviceId, long surgeryId, Instant time, int paramId, float value, long minIntervalMs) {
        Map<Integer, LastRecord> deviceRecords = lastRecordCache.computeIfAbsent(deviceId, k -> new ConcurrentHashMap<>());
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
        BindingInfo info = bindingCache.get(deviceId);
        if (info != null) {
            return info;
        }

        // Check if we recently checked and found no binding (cache for 5 seconds)
        Long lastCheck = noBindingCache.get(deviceId);
        if (lastCheck != null && System.currentTimeMillis() - lastCheck < 5000) {
            return null;
        }

        info = deviceBindingService.getBindingInfo(deviceId);
        if (info != null) {
            bindingCache.put(deviceId, info);
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

        String sql = "INSERT INTO public.waveform (time, treatment_information_id, parameter_id, amplitude) VALUES (?, ?, ?, ?)";
        try {
            jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Waveform w = list.get(i);
                    ps.setTimestamp(1, Timestamp.from(w.getTime()));
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
            log.error("Error batch inserting waveforms", e);
        }
    }

    private void batchInsertParameters(List<WaveformParameter> list) {
        // Update stats
        for (WaveformParameter p : list) {
            parameterStats.computeIfAbsent(p.getTreatmentInformationId(), k -> new AtomicInteger(0)).incrementAndGet();
        }

        String sql = "INSERT INTO public.waveform_parameter (time, treatment_information_id, parameter_id, value) VALUES (?, ?, ?, ?)";
        try {
            jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    WaveformParameter p = list.get(i);
                    ps.setTimestamp(1, Timestamp.from(p.getTime()));
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
            log.error("Error batch inserting parameters", e);
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
}
