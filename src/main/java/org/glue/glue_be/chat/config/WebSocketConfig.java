package org.glue.glue_be.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // STOMP 연결 엔드포인트 등록
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // SockJS 지원 추가 (WebSocket을 지원하지 않는 브라우저에서도 실시간 통신이 가능하도록 하기 위함)
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지를 구독하는 엔드포인트 prefix 설정
        registry.enableSimpleBroker("/topic", "/queue");

        // 메시지를 발행하는 엔드포인트 prefix(접두사) 설정
        // /topic: 1:N 그룹 채팅
        // /queue: 1:1 개인 채팅
        registry.setApplicationDestinationPrefixes("/app");
    }
}