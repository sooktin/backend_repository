package com.sooktin.backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    @NotBlank
    private String message;
    @NotBlank
    private Integer status;
    @NotBlank
    private String accessToken;
    @NotBlank
    private String refreshToken;
}