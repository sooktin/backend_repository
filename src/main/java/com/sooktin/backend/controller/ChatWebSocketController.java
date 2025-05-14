package com.sooktin.backend.controller;

import com.sooktin.backend.domain.ChatMessage;
import com.sooktin.backend.service.ChatService;

import com.sooktin.backend.service.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
    private final ChatService chatService;
    private final PresenceService presenceService;


    @MessageMapping("/chat/sendMessage/{roomId}")
    public void handleChatMessage(@Payload ChatMessage message, @DestinationVariable String roomId) {
        log.info("Received chat message for room {}: sender={}, content={}",
                roomId, message.getSender(), message.getContent());
        chatService.sendMessage(roomId,message);
    }
 
    //트랙킹, mark read message
    @MessageMapping("/chat/view/{roomId}")
    public void handleRoomView(@Payload ChatMessage message, @DestinationVariable String roomId, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("nickname", message.getSender());
        headerAccessor.getSessionAttributes().put("roomId", roomId);

        presenceService.setUserActive(message.getSender(),roomId);

        chatService.markMessageAsRead(roomId, message.getSender());

        chatService.notifyUserViewing(roomId,message);
    }
    //session state cleaning! 사용자가 나갔는지 알려주기?
    @MessageMapping("/chat/exitView/{roomId}")
    public void handleExitView(@Payload ChatMessage message, @DestinationVariable String roomId, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().remove("nickname");
        headerAccessor.getSessionAttributes().remove("roomId");

        presenceService.setUserInactive(message.getSender(),roomId);

        chatService.notifyUserExitedView(roomId,message);
    }
}
