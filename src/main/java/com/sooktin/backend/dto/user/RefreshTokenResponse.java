package com.sooktin.backend.dto.user;

import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.sooktin.backend.dto.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Ref;

@Getter
public class RefreshTokenResponse extends ResponseDto<LoginResponse.TokenDto> {

    public RefreshTokenResponse(int statusCode, String message, LoginResponse.TokenDto data) {
        super(statusCode, message, data);
    }

    public static RefreshTokenResponse success(String accessToken) {
        return new RefreshTokenResponse(200,"토큰 재발급이 성공하였습니다.", new LoginResponse.TokenDto(accessToken,3600,"Bearer"));
    }
    public static RefreshTokenResponse fail() {

        return new RefreshTokenResponse(401,"리프레시 토큰이 유효하지 않습니다.",null);
    }
}
