package com.sooktin.backend.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomStatusUpdateRequest {
    private Boolean isMuted; // null인 경우 기존 값 유지
    private Boolean isPinned; // null인 경우
}