package com.sooktin.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "로그아웃 요청 DTO")
public  class LogoutRequest {
    @NotBlank
    @Schema(description = "email", example = "foo@sookmyung.ac.kr")
    private String email;
}