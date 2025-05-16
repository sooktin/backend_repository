package com.sooktin.backend.global;

import org.springframework.beans.factory.annotation.Value;
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
    
    @Value("${spring.messaging.in-memory:false}")
    private boolean useInMemoryBroker;
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        if (useInMemoryBroker) {
            // 인메모리 메시지 브로커 사용
            registry.enableSimpleBroker("/topic", "/queue");
            registry.setApplicationDestinationPrefixes("/app");
        } else {
            // RabbitMQ 사용 (기존 코드)
            registry.enableStompBrokerRelay("/topic","/queue")
                    .setRelayHost("localhost")
                    .setRelayPort(61613)
                    .setClientLogin("guest")
                    .setClientPasscode("guest")
                    .setSystemLogin("guest")
                    .setSystemPasscode("guest")
                    .setVirtualHost("/");
            registry.setApplicationDestinationPrefixes("/app");
        }
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 순수 WebSocket 엔드포인트 - 모든 오리진 허용
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*");  // 모든 오리진 허용
        
        // SockJS 폴백 지원 추가
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        // 메시지 버퍼 크기 설정 (기본값은 64KB)
        registration.setMessageSizeLimit(128 * 1024); // 128KB
        // 전송 시간 제한 설정 (기본값은 10초)
        registration.setSendTimeLimit(15 * 1000); // 15초
        // 전송 버퍼 크기 제한 설정
        registration.setSendBufferSizeLimit(512 * 1024); // 512KB
        // 하트비트 설정 (서버 ping 주기, 클라이언트 응답 타임아웃)
        registration.setSendTimeLimit(15 * 1000);
        registration.setTimeToFirstMessage(20 * 1000); // 첫 메시지 대기 시간
    }

    @Bean
    public WebSocketHandler chatWebSocketHandler() {
        return new TextWebSocketHandler();
    }
}
