package com.sooktin.backend.service;

import java.util.List;

import com.sooktin.backend.domain.User;
import com.sooktin.backend.global.RabbitConfig;
import com.sooktin.backend.repository.ChatRoomRepository;
import com.sooktin.backend.repository.UserRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.sooktin.backend.domain.ChatMessage.MessageType;
import com.sooktin.backend.domain.ChatMessage;
import com.sooktin.backend.repository.ChatRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ChatService {
    private final SimpMessagingTemplate messagingTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final PresenceService presenceService;
    private static final String CHAT_EXCHANGE = "chat.exchange";
    
    @Value("${spring.messaging.in-memory:false}")
    private boolean useInMemoryBroker;
    
    public ChatService(SimpMessagingTemplate messagingTemplate, 
                      RabbitTemplate rabbitTemplate,
                      ChatRepository chatRepository, 
                      ChatRoomRepository chatRoomRepository,
                      UserRepository userRepository,
                      PresenceService presenceService) {
        this.messagingTemplate = messagingTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.chatRepository = chatRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.userRepository = userRepository;
        this.presenceService = presenceService;
    }

    @Transactional
    public void sendMessage(String roomId, ChatMessage message) {
        try {
            log.info("Received message for roomId: {}", roomId);
            log.info("Message: {}, {}", message.getSender(), message.getContent());

            message.setRoomId(roomId);
            message.setType(MessageType.CHAT);

            ChatMessage savedMessage = chatRepository.save(message);
            log.info("Message saved with ID: {}", savedMessage.getMessageId());

            if (useInMemoryBroker) {
                // 인메모리 브로커 사용 시
                messagingTemplate.convertAndSend("/topic/room/" + roomId, savedMessage);
                log.info("Message sent to /topic/chat/{}", roomId);
                log.info("Message sent to in-memory broker");
            } else {
                // RabbitMQ 사용 시
                rabbitTemplate.convertAndSend(CHAT_EXCHANGE, "room." + roomId, savedMessage);
                log.info("Message sent to RabbitMQ");
            }
        } catch (Exception e) {
            log.error("Error in sendMessage: {}", e.getMessage(), e);
            throw e; // 예외를 다시 던져 상위 호출자에서도 확인 가능
        }
    }

    @Transactional
    public void markMessageAsRead(String roomId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        ChatMessage latestMessage = chatRepository.findTopByRoomIdOrderByTimestampDesc(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방의 최신 메시지를 찾을 수 없습니다."));
    }

    public void notifyUserViewing(String roomId, ChatMessage message) {
        ChatMessage viewingMessage = new ChatMessage();
        viewingMessage.setRoomId(roomId);
        viewingMessage.setType(MessageType.SYSTEM);
        viewingMessage.setSender(message.getSender());
        viewingMessage.setContent(message.getSenderName() + "님이 채팅방을 보고 있습니다.");

        if (useInMemoryBroker) {
            // 인메모리 브로커 사용 시
            messagingTemplate.convertAndSend("/topic/room." + roomId, viewingMessage);
        } else {
            // RabbitMQ 사용 시
            rabbitTemplate.convertAndSend(CHAT_EXCHANGE, "room." + roomId, viewingMessage);
        }
    }

    // 사용자가 채팅 화면 보기를 종료
    public void notifyUserExitedView(String roomId, ChatMessage message) {
        ChatMessage exitViewMessage = new ChatMessage();
        exitViewMessage.setRoomId(roomId);
        exitViewMessage.setType(MessageType.SYSTEM);
        exitViewMessage.setSender(message.getSender());
        exitViewMessage.setContent(message.getSenderName() + "님이 채팅방을 나갔습니다.");

        if (useInMemoryBroker) {
            // 인메모리 브로커 사용 시
            messagingTemplate.convertAndSend("/topic/room." + roomId, exitViewMessage);
        } else {
            // RabbitMQ 사용 시
            rabbitTemplate.convertAndSend(CHAT_EXCHANGE, "room." + roomId, exitViewMessage);
        }
    }

    //사용자가 완전 채팅방 나갔을 때 (멤버십 제거)
    public void handleLeaveRoom(String roomId, ChatMessage message) {
        message.setType(MessageType.LEAVE);
        message.setRoomId(roomId);

        // 메시지 내용이 없으면 기본 메시지 설정
        if (message.getContent() == null || message.getContent().isEmpty()) {
            message.setContent(message.getSenderName() + "님이 채팅방을 나갔습니다.");
        }

        // 메시지 저장
        ChatMessage savedMessage = chatRepository.save(message);

        if (useInMemoryBroker) {
            // 인메모리 브로커 사용 시
            messagingTemplate.convertAndSend("/topic/room." + roomId, savedMessage);
        } else {
            // RabbitMQ 사용 시
            rabbitTemplate.convertAndSend(CHAT_EXCHANGE, "room." + roomId, savedMessage);
        }

        // 사용자를 채팅방에서 제거 (DB에서 멤버십 제거)
        // chatRoomService.removeUserFromRoom(roomId, message.getSender());
    }
    // WebSocket 연결이 끊겼을 때 호출되는 메서드
    public void handleDisconnect(String roomId, ChatMessage message) {
        // 연결 끊김 처리 (일시적 연결 끊김으로 간주)
        message.setType(MessageType.LEAVE);
        message.setRoomId(roomId);
        message.setContent(message.getSenderName() + "님의 연결이 끊겼습니다.");

        // 메시지 저장
        ChatMessage savedMessage = chatRepository.save(message);

        if (useInMemoryBroker) {
            // 인메모리 브로커 사용 시
            messagingTemplate.convertAndSend("/topic/room." + roomId, savedMessage);
        } else {
            // RabbitMQ 사용 시
            rabbitTemplate.convertAndSend(CHAT_EXCHANGE, "room." + roomId, savedMessage);
        }
    }

    public List<ChatMessage> getLatestMessages(String roomId, int limit) {
        return chatRepository.findLatestMessages(roomId, limit);
    }

}
