package com.sooktin.backend.dto.verification;

import com.sooktin.backend.dto.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class VerficationResponse extends ResponseDto<Boolean> {


    public VerficationResponse(int statusCode, String message, boolean data) {
        super(statusCode, message, data);
    }

    public static VerficationResponse success() {
        return new VerficationResponse(200, "이메일 인증에 성공했습니다.", true);
    }
    public static VerficationResponse invalidCode() {
        return new VerficationResponse(401, "인증 코드가 유효하지 않습니다.", false);
    }
    public static VerficationResponse expired() {
        return new VerficationResponse(410, "인증 코드가 만료되었습니다.",false);
    }
}
