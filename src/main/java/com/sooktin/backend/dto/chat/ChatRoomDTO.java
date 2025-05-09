package com.sooktin.backend.dto.chat;

import com.sooktin.backend.domain.ChatRoom;
import com.sooktin.backend.domain.UserChatRoom;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class ChatRoomDTO {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
    private String lastMessagePreview;
    private boolean isDirectMessage = true;
    private List<ParticipantDTO> participant;

    public ChatRoomDTO(ChatRoom chatRoom) {
        this.id = chatRoom.getId();
        this.name = chatRoom.getName();
        this.createdAt = chatRoom.getCreatedAt();
        this.lastMessageAt = chatRoom.getLastMessageAt();
        this.lastMessagePreview = chatRoom.getLastMessagePreview();
        this.isDirectMessage = chatRoom.isDirectMessage();
        this.participant = chatRoom.getUserChatRooms().stream()
                .map(ParticipantDTO::new)
                .collect(Collectors.toList());
    }

    @Data
    @NoArgsConstructor
    static class ParticipantDTO {
        private Long userId;
        private String nickname;

        public ParticipantDTO(UserChatRoom userChatRoom) {
            this.userId = userChatRoom.getUser().getId();
            this.nickname = userChatRoom.getUser().getNickname();
        }
    }
}
