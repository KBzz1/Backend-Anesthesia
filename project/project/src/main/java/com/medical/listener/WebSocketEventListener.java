package com.medical.listener;

import com.medical.utils.AreaConnectionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final AreaConnectionManager connectionManager;

    /**
     * 监听WebSocket连接建立事件
     * 仅记录日志，不做区域注册
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        log.info("新的 WebSocket 连接建立，会话ID: {}", sessionId);
        /* 说明：
         * 此时不注册区域连接，因为：
         * 1. 客户端订阅时会自动注册（通过订阅地址识别）
         * 2. 客户端发送消息时会自动注册（通过消息内容识别）
         *
         * 这样避免了：
         * - 客户端需要在连接时传递额外参数
         * - 服务器需要解析连接头
         * - 代码逻辑更简洁
         */
    }

    /**
     * 监听WebSocket连接断开事件
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        // 获取断开的区域ID（用于日志）
        String areaId = connectionManager.getAreaId(sessionId);
        if (areaId != null) {
            // 区域管理更新，注销区域连接
            connectionManager.unregisterArea(sessionId);
        } else {
            log.info("未知区域断开连接，会话ID: {}", sessionId);
        }
    }

    /**
     * 监听客户端订阅事件
     */
    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();
        String sessionId = headerAccessor.getSessionId();
        log.debug("会话 {} 订阅: {}", sessionId, destination);
        /* 可选：从订阅地址中提取区域ID
         * 例如：订阅 /data/area/d
         * 可以提取出 "d" 作为区域ID
         */
        if (destination != null && destination.startsWith("/data/area/")) {
            String areaId = destination.substring("/data/area/".length());
            // 验证区域ID不为空
            if (!areaId.isEmpty()) {
                connectionManager.registerArea(areaId, sessionId);
            }
        }
    }
}


