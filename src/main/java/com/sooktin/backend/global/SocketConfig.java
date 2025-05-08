package com.sooktin.backend.global;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class SocketConfig implements WebSocketMessageBrokerConfigurer {
    public void configureMessageBroker(MessageBrokerRegistry registry) {
       registry.enableStompBrokerRelay("/topic", "/queue")
               .setRelayHost("localhost")
               .setRelayPort(61613)
               .setClientLogin("guest")
               .setClientPasscode("guest");

       //registry.setApplicationDestinationPrefixes("/app");
    }

    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat").setAllowedOrigins("*"); //.withSockJS();잠깐뺏어욤
    }


}
