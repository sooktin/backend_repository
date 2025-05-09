package com.sooktin.backend.global;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Configuration
@EnableWebSocketMessageBroker
public class SocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat")
                .setAllowedOrigins("*")
                .withSockJS()
                .setClientLibraryUrl("https://cdn.jsdelivr.net/npm/sockjs-client@1.5.1/dist/sockjs.min.js")  // SockJS 클라이언트 라이브러리 URL 지정
                .setWebSocketEnabled(true)  // WebSocket 활성화
                .setSessionCookieNeeded(false);  // 세션 쿠키 비활성화 (CORS 이슈 방지)
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        // 메시지 버퍼 크기 설정 (기본값은 64KB)
        registration.setMessageSizeLimit(128 * 1024); // 128KB
        // 전송 시간 제한 설정 (기본값은 10초)
        registration.setSendTimeLimit(15 * 1000); // 15초
        // 전송 버퍼 크기 제한 설정
        registration.setSendBufferSizeLimit(512 * 1024); // 512KB
    }

    @Bean
    public WebSocketHandler chatWebSocketHandler() {
        return new TextWebSocketHandler();
    }
}
