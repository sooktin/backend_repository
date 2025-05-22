package com.sooktin.backend.dto.chat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sooktin.backend.domain.ChatRoom;
import com.sooktin.backend.domain.UserChatRoom;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomSummaryDTO {
    private Long roomId;
    private String opponentUserNickname;
    private Long opponentCareerCardId; // 상대방 커리어카드 ID 추가
    private Long opponentUserId;
    @JsonFormat(pattern = "HH:mm a")
    private String createdAt;          // LocalDateTime에서 String으로 변경
    @JsonFormat(pattern = "HH:mm a")
    private String lastMessageAt;      // LocalDateTime에서 String으로 변경
    private String lastMessage;
    private String opponentImageUrl;
    private int unreadCount;


    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ofPattern("HH:mm a"));
    }


    public static ChatRoomSummaryDTO from(
            ChatRoom room,
            String opponentNickname,
            Long opponentId,
            Long opponentCardId,
            String imageUrl,
            int unreadCount) {

        return ChatRoomSummaryDTO.builder()
                .roomId(room.getId())
                .opponentUserNickname(opponentNickname)
                .opponentUserId(opponentId)
                .opponentCareerCardId(opponentCardId)
                .createdAt(formatDateTime(room.getCreatedAt()))
                .lastMessageAt(formatDateTime(room.getLastMessageAt()))
                .lastMessage(room.getLastMessagePreview())
                .opponentImageUrl(imageUrl)
                .unreadCount(unreadCount)
                .build();
    }
}