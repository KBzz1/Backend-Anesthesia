package com.medical.config;

import com.medical.pojo.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * STOMP 客户端：连接 live_dashboard.py 的 STOMP 服务器（8765），
 * 接收并打印数据，同时将主 broker 的数据转发到该连接。
 */
@Slf4j
@Component
public class DashboardStompClient {

    @Value("${dashboard.stomp.url:ws://host.docker.internal:8765/ws}")
    private String stompUrl;

    private WebSocketStompClient stompClient;
    private volatile StompSession session;
    private volatile boolean connecting = false;

    /** 待发送的消息缓冲区 */
    private volatile CopyOnWriteArrayList<Data> pendingMessages = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void connect() {
        WebSocketClient client = new StandardWebSocketClient();
        stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        ensureConnected();
    }

    @Scheduled(fixedDelay = 3000)
    public void ensureConnected() {
        StompSession currentSession = session;
        if (connecting || (currentSession != null && currentSession.isConnected())) {
            return;
        }

        try {
            connecting = true;
            StompSessionHandler sessionHandler = new DashboardSessionHandler();
            StompHeaders connectHeaders = new StompHeaders();
            connectHeaders.setAcceptVersion("1.2");
            connectHeaders.setHeartbeat(new long[]{0, 0});
            connectHeaders.setHost(URI.create(stompUrl).getHost());
            log.info("[DASHBOARD] 正在连接 STOMP 服务器: {}", stompUrl);
            stompClient.connectAsync(stompUrl, new WebSocketHttpHeaders(), connectHeaders, sessionHandler)
                    .whenComplete((ignored, throwable) -> {
                        connecting = false;
                        if (throwable != null) {
                            log.warn("[DASHBOARD] 连接任务失败: {}", throwable.getMessage());
                        }
                    });
        } catch (Exception e) {
            connecting = false;
            log.error("[DASHBOARD] 连接失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 每秒发送一次缓冲区中的消息
     */
    @Scheduled(fixedDelay = 1000)
    public void flushMessages() {
        StompSession currentSession = session;
        if (currentSession == null || !currentSession.isConnected()) {
            return;
        }

        List<Data> toSend = pendingMessages;
        if (toSend.isEmpty()) {
            return;
        }
        pendingMessages = new CopyOnWriteArrayList<>();

        int count = 0;
        for (Data data : toSend) {
            try {
                currentSession.send("/data/sub/all", data);
                count++;
            } catch (Exception e) {
                log.warn("[DASHBOARD] 发送失败: {}", e.getMessage());
            }
        }
        log.info("[DASHBOARD] 发送 {} 条消息到 dashboard", count);
    }

    /**
     * 供外部调用：将数据加入发送缓冲区
     */
    public void sendToDashboard(Data data) {
        pendingMessages.add(data);
    }

    private class DashboardSessionHandler extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession stompSession, StompHeaders connectedHeaders) {
            connecting = false;
            session = stompSession;
            log.info("[DASHBOARD] 已连接到 STOMP 服务器，会话ID: {}", stompSession.getSessionId());
        }

        @Override
        public void handleException(StompSession sess, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            log.error("[DASHBOARD] STOMP 异常: {}", exception.getMessage(), exception);
        }

        @Override
        public void handleTransportError(StompSession sess, Throwable exception) {
            log.warn("[DASHBOARD] 连接断开: {}", exception.getMessage());
            session = null;
            connecting = false;
        }
    }

    @PreDestroy
    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }
}
