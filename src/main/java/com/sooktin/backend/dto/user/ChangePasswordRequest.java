package com.sooktin.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "비번 변경 응답")
public class ChangePasswordRequest {
    private String oldPassword;
    private String newPassword;


}