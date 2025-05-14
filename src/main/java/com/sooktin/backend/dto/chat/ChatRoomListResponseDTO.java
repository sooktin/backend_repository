package com.sooktin.backend.dto.chat;

import com.sooktin.backend.dto.ResponseDto;

import java.util.List;

public class ChatRoomListResponseDTO extends ResponseDto<List<ChatRoomSummaryDTO>> {

    public ChatRoomListResponseDTO(int statusCode, String message, List<ChatRoomSummaryDTO> data) {
        super(statusCode, message, data);
    }

    public static ChatRoomListResponseDTO success(List<ChatRoomSummaryDTO> chatRooms) {
        return new ChatRoomListResponseDTO(200, "채팅방 목록을 성공적으로 가져왔습니다.", chatRooms);
    }
}
