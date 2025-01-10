package com.sooktin.backend.dto.user;

import com.sooktin.backend.dto.ResponseDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse extends ResponseDto<LoginResponse.TokenDto> {
    @Data
    public static class TokenDto {
        private String accessToken;
        private Integer expiresIn;
        private String tokenType;


        public TokenDto(String accessToken, Integer expiresIn, String tokenType) {
            this.accessToken = accessToken;
            this.expiresIn = expiresIn;
            this.tokenType = tokenType;
        }
    }

    public LoginResponse(int statusCode, String message, TokenDto data) {
        super(statusCode, message, data);
    }

    public static LoginResponse success(String accessToken) {
        return new LoginResponse(200, "로그인 되었습니다.", new TokenDto(accessToken, 3600, "Bearer"));
    }

    public static LoginResponse fail() {
        return new LoginResponse(400, "잘못된 접근입니다.", null);
    }


}