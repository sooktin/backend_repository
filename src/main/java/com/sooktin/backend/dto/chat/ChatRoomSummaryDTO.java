package com.sooktin.backend.dto.chat;

import com.sooktin.backend.domain.ChatRoom;
import com.sooktin.backend.domain.UserChatRoom;
import lombok.*;


import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@Builder
public class ChatRoomSummaryDTO {
    private Long roomId;
    private String opponentUserNickname;
    private Long opponentUserId;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
    private String lastMessage;
    private String opponentImageUrl;
    private int unreadCount;



}
