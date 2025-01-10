package com.sooktin.backend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotEmpty(message = "이메일은 공백일 수 없습니다.")
    @Email(message = "이메일 형식을 지켜주세요.")
    private String email;
    @NotEmpty(message = "비밀번호는 공백일 수 없습니다.")
    private String password;
    @NotEmpty(message = "닉네임은 공백일 수 없습니다.")
    private String nickname;

}
