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
    public static VerficationResponse tooManyAttempts() { return new VerficationResponse(429, "인증 시도 횟수 초과입니다.",false); }
    public static VerficationResponse systemError() { return new VerficationResponse(500, "인증 시스템 오류입니다.",false); }
    public static VerficationResponse rateLimited() { return new VerficationResponse(429, "1분에 한 번만 요청 가능합니다.",false); }
    public static VerficationResponse emailSendFailed() { return new VerficationResponse(503, "이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.", false); }

}
