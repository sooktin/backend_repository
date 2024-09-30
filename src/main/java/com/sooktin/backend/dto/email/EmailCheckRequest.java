package com.sooktin.backend.dto.email;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Schema(description = "이메일 확인 여요청")
public class EmailCheckRequest {
    @Schema(description = "확인할 이메일 주소", example = "user@sookmyung.com")
    private String email;
}
