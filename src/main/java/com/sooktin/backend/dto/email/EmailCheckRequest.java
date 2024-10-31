package com.sooktin.backend.dto.email;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "이메일 확인 요")
public class EmailCheckRequest {
    @Schema(description = "email")
    private String email;
}
