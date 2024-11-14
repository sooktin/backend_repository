package com.sooktin.backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterResponse {
    private boolean success;
    private String message;
    private int statusCode;

    public static RegisterResponse success() {
        return new RegisterResponse(true,"회원가입이 완료되었습니다.",201);
    }

    public static RegisterResponse duplicateNickname() {
        return new RegisterResponse(false, "닉네임이 이미 존재합니다.", 400);
    }

    public static RegisterResponse duplicateEmail() {
        return new RegisterResponse(false, "이메일이 이미 존재합니다.", 400);
    }

    public static RegisterResponse passwordMismatch() {
        return new RegisterResponse(false, "비밀번호가 일치하지 않습니다.", 400);
    }
}
