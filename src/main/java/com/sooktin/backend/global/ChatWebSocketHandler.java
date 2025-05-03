package com.sooktin.backend.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Slf4j
public class ChatWebSocketHandler extends AbstractWebSocketHandler {

    private static final Logger logger = Logger.getLogger(ChatWebSocketHandler.class.getName());
    private static final ConcurrentHashMap<String, WebSocketSession> CLIENTS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        CLIENTS.put(session.getId(), session);
        logger.info("New WebSocket connection established: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        CLIENTS.remove(session.getId());
        logger.info("WebSocket connection closed: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 클라이언트로부터 메시지 수신
        String payload = message.getPayload();
        logger.info("Received message: " + payload);

        // 모든 클라이언트에게 메시지 전송
        for (WebSocketSession client : CLIENTS.values()) {
            if (client.isOpen()) {
                client.sendMessage(message);
            }
        }
    }
}