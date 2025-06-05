package org.glue.glue_be.chat.config;

import org.glue.glue_be.auth.jwt.JwtTokenProvider;
import org.glue.glue_be.auth.jwt.JwtValidationType;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketHandler;
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
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new HttpSessionHandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

                        // 쿼리 파라미터에서 토큰 추출
                        String token = extractTokenFromQuery(request);
                        if (token == null) {
                            // Authorization 헤더에서 토큰 추출
                            token = extractTokenFromHeader(request);
                        }

                        if (token != null) {
                            try {
                                JwtValidationType validationType = jwtTokenProvider.validateToken(token);
                                if (validationType == JwtValidationType.VALID_JWT) {
                                    Authentication auth = jwtTokenProvider.getAuthentication(token);
                                    attributes.put("authentication", auth);
                                    log.info("WebSocket 핸드셰이크 인증 성공: {}", auth.getName());
                                    return super.beforeHandshake(request, response, wsHandler, attributes);
                                }
                            } catch (Exception e) {
                                log.error("WebSocket 핸드셰이크 인증 실패: {}", e.getMessage());
                            }
                        }

                        log.warn("WebSocket 핸드셰이크 인증 실패 - 토큰 없음 또는 유효하지 않음");
                        response.setStatusCode(HttpStatus.UNAUTHORIZED);
                        return false;
                    }
                })
                .withSockJS()
                .setStreamBytesLimit(512 * 1024)
                .setHttpMessageCacheSize(1000)
                .setDisconnectDelay(30 * 1000);
    }

    private String extractTokenFromQuery(ServerHttpRequest request) {
        String query = request.getURI().getQuery();
        if (query != null && query.contains("token=")) {
            return query.substring(query.indexOf("token=") + 6);
        }
        return null;
    }

    private String extractTokenFromHeader(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");  // 개별 사용자 메시지용
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                log.debug("WebSocket 메시지 수신: {}", accessor.getCommand());

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authToken = accessor.getFirstNativeHeader("Authorization");
                    log.debug("인증 토큰: {}", authToken != null ? "존재함" : "없음");

                    if (authToken != null && authToken.startsWith("Bearer ")) {
                        String token = authToken.substring(7);

                        try {
                            JwtValidationType validationType = jwtTokenProvider.validateToken(token);
                            log.debug("토큰 유효성: {}", validationType);

                            if (validationType == JwtValidationType.VALID_JWT) {
                                Authentication auth = jwtTokenProvider.getAuthentication(token);
                                SecurityContextHolder.getContext().setAuthentication(auth);
                                accessor.setUser(auth);
                                log.info("WebSocket 인증 성공: {}", auth.getName());
                            } else {
                                log.warn("유효하지 않은 JWT 토큰");
                                throw new RuntimeException("유효하지 않은 JWT 토큰");
                            }
                        } catch (Exception e) {
                            log.error("WebSocket 인증 실패: {}", e.getMessage());
                            throw new RuntimeException("WebSocket 인증 실패", e);
                        }
                    } else {
                        log.warn("Authorization 헤더가 없거나 형식이 잘못됨");
                        throw new RuntimeException("Authorization 헤더가 필요합니다");
                    }
                }

                return message;
            }
        });
    }
}