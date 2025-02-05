package com.sooktin.backend.dto.liked;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LikedResponse {
    private boolean isLiked;       // 좋아요 상태
    private String message;        // 응답 메시지
    private LocalDateTime timestamp = LocalDateTime.now();  // 처리 시간

    public static LikedResponse liked() {
        return new LikedResponse(true, "좋아요 추가되었습니다.");
    }

    public static LikedResponse unliked() {
        return new LikedResponse(false, "좋아요 취소되었습니다.");
    }

    // 생성자 오버로딩
    public LikedResponse(boolean isLiked, String message) {
        this.isLiked = isLiked;
        this.message = message;
    }
}
