package com.sooktin.backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeRequest {
    @NotBlank(message = "비밀번호를 입력하세요")
    private String oldPassword;
    @NotBlank(message = "새로운 비밀번호를 입력하세요")
    private String newPassword;


}