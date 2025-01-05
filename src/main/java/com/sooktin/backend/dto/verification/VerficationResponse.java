package com.sooktin.backend.dto.verification;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerficationResponse {
    private boolean verified;
    private String message;
    private int statusCode;

    public static VerficationResponse success() {
        return new VerficationResponse(true, "이메일 인증에 성공했습니다.", 200);
    }
    public static VerficationResponse invalidCode() {
        return new VerficationResponse(false, "인증 코드가 유효하지 않습니다.", 401);
    }
    public static VerficationResponse expired() {
        return new VerficationResponse(false, "인증 코드가 만료되었습니다.",410);
    }
}
