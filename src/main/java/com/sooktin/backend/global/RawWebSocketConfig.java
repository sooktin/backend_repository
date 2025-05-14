package com.sooktin.backend.global;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Configuration
public class RawWebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(myHandler(), "/echo")
                .setAllowedOrigins("*");
    }
    // ...
    @Bean
    public WebSocketHandler myHandler() {  // 이 함수 이름은 아무거나 가능합니다
        return new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                // 여기서 클라이언트로부터 받은 메시지를 처리합니다
                System.out.println("받은 메시지: " + message.getPayload());

                // 클라이언트에게 응답을 보냅니다
                session.sendMessage(new TextMessage("응답: " + message.getPayload()));
            }
        };
    }
}
