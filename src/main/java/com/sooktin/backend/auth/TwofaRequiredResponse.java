package com.sooktin.backend.auth;

import lombok.Getter;

@Getter
public class TwofaRequiredResponse {
    private String msg = "2FA 인증이 필요합니다.";
}
