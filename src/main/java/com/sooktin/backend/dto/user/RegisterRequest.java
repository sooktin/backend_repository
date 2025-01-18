package com.sooktin.backend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
            message = "비밀번호는 8~20자리이면서 1개 이상의 알파벳, 숫자, 특수문자를 포함해야합니다."
    )
    private String password;
    @NotEmpty(message = "닉네임은 공백일 수 없습니다.")
    private String nickname;

}
