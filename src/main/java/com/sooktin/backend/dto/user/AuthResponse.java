package com.sooktin.backend.dto.user;

import com.sooktin.backend.dto.ResponseDto;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class AuthResponse extends ResponseDto<Map<String,String>> {

    public AuthResponse(int statusCode, String message, String accessToken) {
        super(statusCode, message, createToken(accessToken)); // 부모 생성자 호출
    }

    private static Map<String, String> createToken(String accessToken) {
        Map<String, String> token = new HashMap<>();
        token.put("access_token", accessToken);
        return token;
    }
}
