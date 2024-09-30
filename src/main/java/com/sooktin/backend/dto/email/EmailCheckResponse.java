package com.sooktin.backend.dto.email;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@Schema(description = "이메일 확인 응답")
public class EmailCheckResponse {
    @Schema(description = "이메일 존재 여부", example = "true")
    private boolean exists;
    @Schema(description = "응답 메시지", example = "이메일이 확인되었습니다. 비밀번호를 입력해주세요.")
    private String message;
}
