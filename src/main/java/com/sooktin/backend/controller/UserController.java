package com.sooktin.backend.controller;

import com.sooktin.backend.auth.AuthResponse;
import com.sooktin.backend.auth.AuthenticationResult;
import com.sooktin.backend.auth.AuthenticationStatus;
import com.sooktin.backend.auth.TwofaRequiredResponse;
import com.sooktin.backend.domain.User;

import com.sooktin.backend.dto.email.EmailCheckRequest;
import com.sooktin.backend.dto.email.EmailCheckResponse;

import com.sooktin.backend.service.AuthenticationService;
import com.sooktin.backend.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.web.csrf.InvalidCsrfTokenException;

import com.sooktin.backend.dto.UserDto;
import com.sooktin.backend.dto.email.EmailCheckRequest;
import com.sooktin.backend.dto.email.EmailCheckResponse;
import com.sooktin.backend.service.AuthenticationService;
import com.sooktin.backend.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {
    private final AuthenticationService authenticationService;

    private final UserService userService;


    @Operation(summary = "회원 가입 API", description = "회원 가입에 성공하면 이메일 인증(2fa)을 거칩니다.")
    @ApiResponse(responseCode = "200", description = "2fa로 넘어갑니다. 추후 201 코드로 바뀔듯요")
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);
            return ResponseEntity.ok("회원가입에 성공하셨습니다. 이메일 인증을 위하여 이메일함을 확인해주세요. ");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @Operation(summary = "이메일 인증 API", description = "해당되는 이메일로 링크가 전달됩니다.")
    @GetMapping("/2fa")
    public ResponseEntity<String> confirmEmail(@RequestParam("token") String token) {
        try {
            String result = userService.confirmEmail(token);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("에러 발생");
        }
    }


    @Operation(summary = "우선 이메일 확인 API", description = "이메일이 User DB에 있는지 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이메일이 존재",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = EmailCheckResponse.class))}
            ),
            @ApiResponse(responseCode = "404", description = "이메일 없음 ㅠㅠ",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = EmailCheckResponse.class))}
                    )
    })
    @PostMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestBody EmailCheckRequest request) {
        boolean emailExists = authenticationService.checkEmailExists(request.getEmail());
        if (emailExists) {
            return ResponseEntity.ok(new EmailCheckResponse(true, "이메일이 확인되었습니다. 비밀번호를 입력해주세요."));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new EmailCheckResponse(false, "이메일이 존재하지 않아 회원가입으로 이동합니다."));
        }
    }

    @Operation(summary = "로그인 API", description = "2차인증이 필요하다면 2차인증 수속을 다시 밟고 이미 되었다면 JWT 토큰을 반환합니다.")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        AuthenticationResult result = authenticationService.authenticate(user.getEmail(), user.getPassword());
        if (result.getStatus() == AuthenticationStatus.REQUIRES_2FA) {
            return ResponseEntity.ok(new TwofaRequiredResponse());
        } else if (result.getStatus() == AuthenticationStatus.AUTHENTICATED) {
            return ResponseEntity.ok(new AuthResponse(result.getToken()));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("다시 접속해주세요.");
        }



    }






    @Operation(summary = "로그아웃 API", description = "이미 로그아웃되었으므로 아무것도 반환하지 않습니다.")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody UserDto.LogoutRequestDto logoutRequestDto) {
        authenticationService.logout(logoutRequestDto.getEmail());
        return ResponseEntity.ok().build();
    }
}
