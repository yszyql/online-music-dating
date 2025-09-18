package org.x.backend.config;

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
        // 启用内存Broker，也可替换成 RabbitMQ、Redis 等消息中间件
        config.enableSimpleBroker("/topic", "/queue"); // 消息代理前缀
        // 配置用户订阅前缀，前端订阅用户专属消息时用 /user
        config.setUserDestinationPrefix("/user");
        config.setApplicationDestinationPrefixes("/app"); // 应用前缀
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat-websocket")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // 启用 SockJS  fallback
    }
}