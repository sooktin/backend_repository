package com.sooktin.backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NicknameRequest {
    @NotBlank(message = "닉네임을 입력해주세요")
    @Size(min = 2, max = 10,message = "닉네임은 2~10자 제한합니다.")
    private String nickname;
}
