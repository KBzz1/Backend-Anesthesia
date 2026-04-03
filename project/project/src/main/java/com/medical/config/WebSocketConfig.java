package com.medical.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 客户端订阅前缀（广播消息时使用）
        config.enableSimpleBroker("/data","/queue")
         /*
         *   setHeartbeatValue - 心跳检测
         *   [10000, 10000] 表示：
         *   - 服务器每10秒向客户端发送心跳
         *   - 期望客户端每10秒发送心跳
         *   如果超时未收到心跳，连接会被断开
         */
            .setHeartbeatValue(new long[]{10000, 10000})
            .setTaskScheduler(heartBeatScheduler());
        // 客户端发送消息前缀
        // convertAndSend实际内部逻辑：
        // 1. 查找所有当前订阅了该topic的活跃会话
        // 2. 如果找到订阅者：向每个订阅者发送消息
        // 3. 如果没找到订阅者：直接丢弃消息，无任何处理
        // 4. 不会检查历史订阅记录，只关心当前状态
        config.setApplicationDestinationPrefixes("/data");
        // 点对点消息前缀（可选）
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 握手连接地址
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*"); // 允许跨域
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        // 配置传输参数
        registration
                .setMessageSizeLimit(128 * 1024)      // 消息大小限制：128KB
                .setSendBufferSizeLimit(512 * 1024)   // 发送缓冲区：512KB
                .setSendTimeLimit(20 * 1000);         // 发送超时：20秒
    }
    /**
     * WebSocket 心跳任务调度器
     * 用于定时发送心跳帧，保持连接活跃
     */
    @Bean
    public TaskScheduler heartBeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("websocket-heartbeat-");
        scheduler.setDaemon(true);  // 设置为守护线程
        scheduler.initialize();
        return scheduler;
    }
}
