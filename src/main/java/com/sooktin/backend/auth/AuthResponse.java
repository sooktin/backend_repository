package com.sooktin.backend.auth;

import lombok.Getter;

@Getter
public class AuthResponse {
    private String AccessToken;

    public AuthResponse(String token) {
        this.AccessToken = token;
    }
}
