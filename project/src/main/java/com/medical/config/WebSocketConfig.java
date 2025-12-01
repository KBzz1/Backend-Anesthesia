package com.medical.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple memory-based message broker to carry the greeting messages back to the client on destinations prefixed with "/data"
        config.enableSimpleBroker("/data");
        // Designates the "/app" prefix for messages that are bound for methods annotated with @MessageMapping.
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Registers the "/ws" endpoint, enabling SockJS fallback options so that alternate transports may be used if WebSocket is not available.
        // The SockJS client will attempt to connect to "/ws".
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
        // Also add a raw WebSocket endpoint for non-SockJS clients (like the one in the HTML example which uses 'new WebSocket(wsUrl)')
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
    }
}
