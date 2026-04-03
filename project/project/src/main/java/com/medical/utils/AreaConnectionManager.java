package com.medical.utils;

import com.medical.utils.enums.AreaEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
// 各区域连接管理，注意只局限于管理代表指定区域（订阅）的连接，其他连接不进行维护管理
public class AreaConnectionManager {

    // 区域ID -> 会话ID 映射
    private final Map<String, String> areaSessionMap = new ConcurrentHashMap<>();

    // 会话ID -> 区域ID 映射
    private final Map<String, String> sessionAreaMap = new ConcurrentHashMap<>();

    // 区域最后活跃时间
    private final Map<String, LocalDateTime> areaLastActiveTime = new ConcurrentHashMap<>();

    /**
     * 注册区域连接，当某个区域的客户端连接成功时调用
     */
    public void registerArea(String areaId, String sessionId) {
        // 双向映射：既能通过区域找会话，也能通过会话找区域
        if (!AreaEnum.isValidAreaId(areaId)) {
            log.error("区域管理注册失败：无效的区域ID: {}", areaId);
            return;
        }
        // 检查该区域是否已绑定其他会话
        String existingSessionId = areaSessionMap.get(areaId);
        if (existingSessionId != null && !existingSessionId.equals(sessionId)) {
            log.warn("区域 {} 已绑定会话 {}，将使旧绑定失效", areaId, existingSessionId);
            // 移除旧的双向绑定
            areaSessionMap.remove(areaId);
            sessionAreaMap.remove(existingSessionId);  // ✅ 使用之前获取的值
        }
        // 检查该会话是否已绑定其他区域
        String oldAreaId = sessionAreaMap.get(sessionId);
        if (oldAreaId != null) {
            log.warn("会话 {} 已绑定区域 {}，将使旧绑定失效", sessionId, oldAreaId);
            // 移除旧的双向绑定
            sessionAreaMap.remove(sessionId);
            areaSessionMap.remove(oldAreaId);
        }
        // 建立新的双向绑定
        areaSessionMap.put(areaId, sessionId);
        sessionAreaMap.put(sessionId, areaId);
        areaLastActiveTime.put(areaId, LocalDateTime.now());
        log.info("区域 {} 通过订阅注册成功，会话ID: {}", areaId, sessionId);
    }

    /**
     * 注销区域连接，当客户端断开连接时调用
     */
    public void unregisterArea(String sessionId) {
        String areaId = sessionAreaMap.remove(sessionId);
        if (areaId != null) {
            areaSessionMap.remove(areaId);
            areaLastActiveTime.remove(areaId);
            log.info("区域 {} 已断开连接，会话ID: {}", areaId, sessionId);
        }
    }

    /**
     * 更新区域活跃时间
     */
    public void updateActiveTime(String areaId) {
        areaLastActiveTime.put(areaId, LocalDateTime.now());
    }

    /**
     * 检查区域是否在线
     */
    public boolean isAreaOnline(String areaId) {
        return areaSessionMap.containsKey(areaId);
    }

    /**
     * 获取区域的会话ID
     */
    public String getSessionId(String areaId) {
        return areaSessionMap.get(areaId);
    }

    /**
     * 根据会话ID获取区域ID
     */
    public String getAreaId(String sessionId) {
        return sessionAreaMap.get(sessionId);
    }

    /**
     * 获取所有在线区域
     */
    public Map<String, String> getAllOnlineAreas() {
        return new ConcurrentHashMap<>(areaSessionMap);
    }
}


