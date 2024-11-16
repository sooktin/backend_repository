package com.sooktin.backend.dto.user;

import com.nimbusds.oauth2.sdk.token.RefreshToken;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Ref;

@Getter
@AllArgsConstructor
public class RefreshTokenResponse {
    private String message;
    private int statusCode;
    private String accessToken;

    public static RefreshTokenResponse success(String accessToken) {
        return new RefreshTokenResponse("토큰 재발급이 성공하였습니다.",200, accessToken);
    }
    public static RefreshTokenResponse fail() {
        return new RefreshTokenResponse("리프레시 토큰이 유효하지 않습니다.",401,null);
    }
}
