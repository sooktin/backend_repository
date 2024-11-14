package com.sooktin.backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String email;
    private String password;
    private String confirmPassword;
    private String nickname;

    public boolean isPasswordMatch() {
        return password != null && password.equals(confirmPassword);
    }
}
