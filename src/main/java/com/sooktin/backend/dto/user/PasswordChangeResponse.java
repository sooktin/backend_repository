package com.sooktin.backend.dto.user;

import com.sooktin.backend.dto.ResponseDto;
import lombok.*;

@Getter
@Setter
public class PasswordChangeResponse extends ResponseDto {

    public PasswordChangeResponse(int statusCode, String message, Object data) {
        super(statusCode, message, data);
    }
}