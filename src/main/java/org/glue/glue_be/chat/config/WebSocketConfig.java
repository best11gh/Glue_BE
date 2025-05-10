package org.glue.glue_be.chat.config;

import org.glue.glue_be.auth.jwt.JwtTokenProvider;
import org.glue.glue_be.auth.jwt.JwtValidationType;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // STOMP 연결 엔드포인트 등록
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // SockJS 지원 추가
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지를 구독하는 엔드포인트 prefix 설정
        registry.enableSimpleBroker("/topic", "/queue");

        // 메시지를 발행하는 엔드포인트 prefix(접두사) 설정
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                // CONNECT 메시지인 경우에만 인증 처리
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Authorization 헤더에서 JWT 토큰 추출
                    String authToken = accessor.getFirstNativeHeader("Authorization");

                    if (authToken != null && authToken.startsWith("Bearer ")) {
                        String token = authToken.substring(7);

                        try {
                            // 토큰 유효성 검증
                            JwtValidationType validationType = jwtTokenProvider.validateToken(token);

                            if (validationType == JwtValidationType.VALID_JWT) {
                                // 토큰에서 인증 정보 추출
                                Authentication auth = jwtTokenProvider.getAuthentication(token);

                                // SecurityContext에 인증 정보 저장
                                SecurityContextHolder.getContext().setAuthentication(auth);
                                accessor.setUser(auth);
                            }
                        } catch (Exception e) {
                            // 예외 처리
                        }
                    }
                }

                return message;
            }
        });
    }
}