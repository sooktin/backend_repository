package com.sooktin.backend.dto.liked;

import com.sooktin.backend.dto.ResponseDto;
import com.sooktin.backend.dto.user.LoginResponse;
import lombok.*;

import java.time.LocalDateTime;

@Getter
public class LikedResponse extends ResponseDto<LikedResponse.LikedDto> {
    public LikedResponse(int statusCode, String message, LikedDto data) {
        super(statusCode, message, data); // 명시적으로 상위 클래스의 생성자 호출
    }
    @Data
    public static class LikedDto {
        private final boolean isLiked;
        private final LocalDateTime timestamp;

        public LikedDto(boolean isLiked, LocalDateTime timestamp) {
            this.isLiked = isLiked;
            this.timestamp = timestamp;
        }
    }

    // 좋아요 추가 응답
    public static LikedResponse liked() {
        return new LikedResponse(
                200,
                "좋아요 추가되었습니다",
                new LikedDto(true, LocalDateTime.now())
        );
    }
    // 좋아요 취소 응답
    public static LikedResponse unliked() {
        return new LikedResponse(
                200,
                "좋아요 취소되었습니다",
                new LikedDto(false, LocalDateTime.now())
        );
}

//    // 인증 실패 응답
//    public static LikedResponse unauthorized() {
//        return new LikedResponse(
//                401,
//                "잘못된 JWT 토큰입니다",
//                new LikedDto(false, LocalDateTime.now())
//        );
//    }

//    // 잘못된 요청 응답
//    public static LikedResponse badRequest(String message) {
//        return new LikedResponse(
//                400,
//                message,
//                new LikedDto(false, LocalDateTime.now())
//        );
//    }
}
