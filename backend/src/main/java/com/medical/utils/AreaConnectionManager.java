package com.medical.utils;

import com.medical.utils.enums.AreaEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class AreaConnectionManager {

    private final Map<String, String> areaSessionMap = new ConcurrentHashMap<>();
    private final Map<String, String> sessionAreaMap = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> areaLastActiveTime = new ConcurrentHashMap<>();

    public void registerArea(String areaId, String sessionId) {
        if (!AreaEnum.isValidAreaId(areaId)) {
            log.error("区域管理注册失败：无效的区域ID: {}", areaId);
            return;
        }

        String existingSessionId = areaSessionMap.get(areaId);
        if (existingSessionId != null && !existingSessionId.equals(sessionId)) {
            areaSessionMap.remove(areaId);
            sessionAreaMap.remove(existingSessionId);
        }

        String oldAreaId = sessionAreaMap.get(sessionId);
        if (oldAreaId != null) {
            sessionAreaMap.remove(sessionId);
            areaSessionMap.remove(oldAreaId);
        }

        areaSessionMap.put(areaId, sessionId);
        sessionAreaMap.put(sessionId, areaId);
        areaLastActiveTime.put(areaId, LocalDateTime.now());
        log.info("区域 {} 通过订阅注册成功，会话ID: {}", areaId, sessionId);
    }

    public void unregisterArea(String sessionId) {
        String areaId = sessionAreaMap.remove(sessionId);
        if (areaId != null) {
            areaSessionMap.remove(areaId);
            areaLastActiveTime.remove(areaId);
            log.info("区域 {} 已断开连接，会话ID: {}", areaId, sessionId);
        }
    }

    public void updateActiveTime(String areaId) {
        areaLastActiveTime.put(areaId, LocalDateTime.now());
    }

    public boolean isAreaOnline(String areaId) {
        return areaSessionMap.containsKey(areaId);
    }

    public String getSessionId(String areaId) {
        return areaSessionMap.get(areaId);
    }

    public String getAreaId(String sessionId) {
        return sessionAreaMap.get(sessionId);
    }

    public Map<String, String> getAllOnlineAreas() {
        return new ConcurrentHashMap<>(areaSessionMap);
    }
}
