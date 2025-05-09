package com.sooktin.backend.service;

import java.util.List;

import com.sooktin.backend.domain.User;
import com.sooktin.backend.repository.ChatRoomRepository;
import com.sooktin.backend.repository.UserRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.sooktin.backend.domain.ChatMessage.MessageType;
import com.sooktin.backend.domain.ChatMessage;
import com.sooktin.backend.repository.ChatRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final SimpMessagingTemplate messagingTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final PresenceService presenceService;
    private final String EXCHANGE_NAME = "chat.exchange";
    
    public void sendMessage(String roomId, ChatMessage message) {
        message.setRoomId(roomId);
        message.setType(MessageType.CHAT);

        ChatMessage savedMessage = chatRepository.save(message);

        //WebSocket을 통한 클라이언트 전송
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, savedMessage);
        
        //RMQ를 통한 서버 간 전송
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, "room." + roomId, savedMessage);

        //TODO : SSE
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

        // SSE가 필요하지안다고?
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, viewingMessage);
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, "room." + roomId, viewingMessage);
    }

    // 사용자가 채팅 화면 보기를 종료
    public void notifyUserExitedView(String roomId, ChatMessage message) {
        ChatMessage exitViewMessage = new ChatMessage();
        exitViewMessage.setRoomId(roomId);
        exitViewMessage.setType(MessageType.SYSTEM);
        exitViewMessage.setSender(message.getSender());
        exitViewMessage.setContent(message.getSenderName() + "님이 채팅방을 나갔습니다.");

        messagingTemplate.convertAndSend("/topic/chat/" + roomId, exitViewMessage);
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

        // WebSocket으로 알림 전송
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, savedMessage);

        // 필요하다면 RabbitMQ로 메시지 전파
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, "room." + roomId, savedMessage);

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

        // WebSocket으로 알림 전송
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, savedMessage);

        // 필요하다면 RabbitMQ로 메시지 전파
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, "room." + roomId, savedMessage);
    }
    
    public List<ChatMessage> getLatestMessages(String roomId, int limit) {
        return chatRepository.findLatestMessages(roomId, limit);
    }

}
