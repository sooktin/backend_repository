package com.sooktin.backend.dto.email;

import com.sooktin.backend.dto.ResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
public class EmailCheckResponse extends ResponseDto<Boolean> {

  public EmailCheckResponse(int statusCode, String message, boolean exists) {
        super(statusCode, message, exists);
    }

    public static EmailCheckResponse loginRequired() {
        return new EmailCheckResponse(200,"로그인을 진행해주세요",true);
    }
    public static EmailCheckResponse registerRequired() {
        return new EmailCheckResponse(202,"회원가입을 진행해주세요",false);
    }
    public static EmailCheckResponse accountRequired() {
        return new EmailCheckResponse(401,"계정이 정지되었습니다. 고객센터에 문의해주세요.",false);
    }


}
