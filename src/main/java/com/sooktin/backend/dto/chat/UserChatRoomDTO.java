package com.sooktin.backend.dto.chat;

import com.sooktin.backend.domain.UserChatRoom;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Data
public class UserChatRoomDTO {
    private Long id;
    private Long userId;
    private Long roomId;
    private String roomName;
    private Long lastReadMessageId;
    private String lastReadAt;      // LocalDateTime에서 String으로 변경
    private Integer unreadCount;
    private String joinedAt;        // LocalDateTime에서 String으로 변경
    private Boolean isMuted;
    private Boolean isPinned;
    private String lastMessagePreview;
    private String lastMessageAt;   // 추가된 필드

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm a", Locale.ENGLISH);

    public static UserChatRoomDTO from(UserChatRoom userChatRoom) {
        UserChatRoomDTO dto = new UserChatRoomDTO();
        dto.setId(userChatRoom.getId());
        dto.setUserId(userChatRoom.getUser().getId());
        dto.setRoomId(userChatRoom.getRoom().getId());
        dto.setRoomName(userChatRoom.getRoom().getName());
        dto.setLastReadMessageId(userChatRoom.getLastReadMessageId());
        dto.setLastReadAt(formatDateTime(userChatRoom.getLastReadAt()));
        dto.setUnreadCount(userChatRoom.getUnreadCount());
        dto.setJoinedAt(formatDateTime(userChatRoom.getJoinedAt()));
        dto.setIsMuted(userChatRoom.getIsMuted());
        dto.setIsPinned(userChatRoom.getIsPinned());
        dto.setLastMessagePreview(userChatRoom.getRoom().getLastMessagePreview());
        dto.setLastMessageAt(formatDateTime(userChatRoom.getRoom().getLastMessageAt()));

        return dto;
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(FORMATTER) : null;
    }
}