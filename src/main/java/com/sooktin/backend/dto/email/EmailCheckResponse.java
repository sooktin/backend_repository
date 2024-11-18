package com.sooktin.backend.dto.email;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@Schema(description = "이메일 확인 응답")
public class EmailCheckResponse {
    @Schema(description = "이메일 존재 여부")
    private boolean exists;
    @Schema(description = "응답 메시지")
    private String message;
    @Schema(description = "응답 코드")
    private int statusCode;

    public static EmailCheckResponse loginRequired() {
        return new EmailCheckResponse(true,"로그인을 진행해주세요",200);
    }
    public static EmailCheckResponse registerRequired() {
        return new EmailCheckResponse(false,"회원가입을 진행해주세요",202);
    }
    public static EmailCheckResponse accountRequired() {
        return new EmailCheckResponse(false,"계정이 정지되었습니다. 고객센터에 문의해주세요.",401);
    }
    public static EmailCheckResponse badRequestRequired() {
        return new EmailCheckResponse(false,"잘못된 요청입니다.",404);
    }
    public static EmailCheckResponse serverRequired() {
        return new EmailCheckResponse(false,"내부 서버 오류입니다. 다시 접속해주세요.",500);
    }
}
