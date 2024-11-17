package com.sooktin.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "2FA 필요 응답")
public class TwofaRequiredResponse {
    private String message = "2FA 인증이 필요합니다.";
}
