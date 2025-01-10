package com.sooktin.backend.dto.user;

import com.sooktin.backend.dto.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class RegisterResponse extends ResponseDto<Boolean> {


    public RegisterResponse(int statusCode, String message, Boolean success) {
        super(statusCode, message, success);
    }

    public static RegisterResponse success() {
        return new RegisterResponse(201,"회원가입이 완료되었습니다.",true);
    }

    public static RegisterResponse duplicateNickname() {
        return new RegisterResponse(400, "닉네임이 이미 존재합니다.", false);
    }

    public static RegisterResponse duplicateEmail() {
        return new RegisterResponse(400, "이메일이 이미 존재합니다.", false);
    }

    public static RegisterResponse passwordMismatch() {
        return new RegisterResponse(400, "비밀번호가 일치하지 않습니다.", false);
    }
}
