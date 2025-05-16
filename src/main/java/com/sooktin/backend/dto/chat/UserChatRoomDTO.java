package com.sooktin.backend.dto.chat;

import com.sooktin.backend.domain.UserChatRoom;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserChatRoomDTO {
    private Long id;
    private Long userId;
    private Long roomId;
    private String roomName;
    private Long lastReadMessageId;
    private LocalDateTime lastReadAt;
    private Integer unreadCount;
    private LocalDateTime joinedAt;
    private Boolean isMuted;
    private Boolean isPinned;
    private String lastMessagePreview;

    /**
     *
     * 정적 팩토리 메서드일시 여러 생성패턴을 만들수있음
     *
     */
    public static UserChatRoomDTO from(UserChatRoom userChatRoom) {
        UserChatRoomDTO dto = new UserChatRoomDTO();
        dto.setId(userChatRoom.getId());
        dto.setUserId(userChatRoom.getUser().getId());
        dto.setRoomId(userChatRoom.getRoom().getId());
        dto.setRoomName(userChatRoom.getRoom().getName());
        dto.setLastReadMessageId(userChatRoom.getLastReadMessageId());
        dto.setLastReadAt(userChatRoom.getLastReadAt());
        dto.setUnreadCount(userChatRoom.getUnreadCount());
        dto.setJoinedAt(userChatRoom.getJoinedAt());
        dto.setIsMuted(userChatRoom.getIsMuted());
        dto.setIsPinned(userChatRoom.getIsPinned());
        dto.setLastMessagePreview(userChatRoom.getRoom().getLastMessagePreview());

        return dto;
    }

}
