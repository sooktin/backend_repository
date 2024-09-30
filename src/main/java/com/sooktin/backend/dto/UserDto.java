package com.sooktin.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NonNull;
import jakarta.validation.constraints.Email;
import lombok.Setter;


public class UserDto {
    @Getter
    @Setter
     public static class LoginRequestDto{
         @NotBlank(message = "이메일을 입력해주세요")
         @Email
         private String email;

         @NotBlank(message = "비밀번호을 입력해주세요")
         private String password;
     }

     @Getter
     @Setter
     public static class LoginResponseDto{
         @NotBlank
         private String message;
         @NotBlank
         private Integer status;
         @NotBlank
         private String accessToken;
         @NotBlank
         private String refreshToken;
     }

     @Getter
     @Setter
     public static class LogoutRequestDto{
        @NotBlank
         private String email;
      }

}
