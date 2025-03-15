package com.sooktin.backend.dto.usernote;

import com.sooktin.backend.dto.ResponseDto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class FindMyUsernoteWithJWTResponse extends ResponseDto<FindMyUsernoteWithJWTResponse.FindMyUsernoteDto> {
    @Data
    public static class FindMyUsernoteDto {
        private Long id;
        private String title;
        private String content;
        // private Integer likes;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
    }
    public FindMyUsernoteWithJWTResponse(int statusCode, String message, FindMyUsernoteDto data) {
        super(statusCode, message, data);
    }
}
