package com.example.shopping3.config;

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
        // 启用简单 Broker，支持 /chat 和 /order 前缀
        config.enableSimpleBroker("/chat", "/order");
        // 设置应用目的地前缀
        config.setApplicationDestinationPrefixes("/app");
        // 设置用户目的地前缀（可选，用于点对点消息）
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
    // 添加 WebSocket 端点，允许跨域，同时支持原生WebSocket和SockJS
    registry.addEndpoint("/ws-chat")
            .setAllowedOriginPatterns("*");
    // 单独添加SockJS支持作为降级方案
    registry.addEndpoint("/ws-chat")
            .setAllowedOriginPatterns("*")
            .withSockJS();
}
}