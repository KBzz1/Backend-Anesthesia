package com.medical.listener;

import com.medical.utils.AreaConnectionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final AreaConnectionManager connectionManager;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("新的 WebSocket 连接建立，会话ID: {}", headerAccessor.getSessionId());
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String areaId = connectionManager.getAreaId(sessionId);
        if (areaId != null) {
            connectionManager.unregisterArea(sessionId);
        } else {
            log.info("未知区域断开连接，会话ID: {}", sessionId);
        }
    }

    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();
        String sessionId = headerAccessor.getSessionId();
        if (destination != null && destination.startsWith("/data/area/")) {
            String areaId = destination.substring("/data/area/".length());
            if (!areaId.isEmpty()) {
                connectionManager.registerArea(areaId, sessionId);
            }
        }
    }
}
