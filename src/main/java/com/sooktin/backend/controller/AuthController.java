package com.sooktin.backend.controller;

import com.sooktin.backend.auth.AuthResponse;
import com.sooktin.backend.auth.AuthenticationResult;
import com.sooktin.backend.auth.JwtUtil;

import com.sooktin.backend.dto.email.EmailCheckRequest;
import com.sooktin.backend.dto.email.EmailCheckResponse;

import com.sooktin.backend.dto.user.*;
import com.sooktin.backend.dto.verification.VerficationResponse;
import com.sooktin.backend.dto.verification.VerificationRequest;
import com.sooktin.backend.service.AuthenticationService;
import com.sooktin.backend.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import org.springframework.web.bind.annotation.*;

import static com.sooktin.backend.auth.AuthenticationStatus.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @GetMapping("/id")
    public String id() {
        return "hey 나연";
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerUser(@RequestBody RegisterRequest request) {
        try {
            userService.registerUser(request);
            return ResponseEntity.status(201).body(RegisterResponse.success());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(RegisterResponse.passwordMismatch());
        } catch (Exception e) {
            if (e.getMessage().contains("닉네임")) {
                return ResponseEntity.badRequest().body(RegisterResponse.duplicateNickname());
            } else if (e.getMessage().contains("이메일")) {
                return ResponseEntity.badRequest().body(RegisterResponse.duplicateEmail());
            }
            return ResponseEntity.badRequest()
                    .body(new RegisterResponse(false, e.getMessage(), 400));
        }
    }


    @Operation(summary = "이메일 인증 API", description = "해당되는 이메일로 초대장이 전달됩니다.")
    @PostMapping("/verify")
    public ResponseEntity<VerficationResponse> verifyEmail(@RequestBody VerificationRequest request) {
        VerficationResponse response = userService.verifyEmail(request.getEmail(), request.getCode());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/send-verification")
    public ResponseEntity<?> sendVerificationCode(@RequestBody SendVerificationRequest request) {
        try {
            userService.sendVerificationCode(request.getEmail());
            return ResponseEntity.ok("인증 코드가 발송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("인증 코드 발송에 실패했습니다.");
        }
    }

    @PostMapping("/check-email")
    public ResponseEntity<EmailCheckResponse> checkEmail(@RequestBody EmailCheckRequest request) {
        EmailCheckResponse response = userService.checkEmail(request.getEmail());
        return ResponseEntity.status(response.getStatusCode()).body(response);

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(LoginRequest loginRequest) {
        AuthenticationResult result = authenticationService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());
        if (result.getStatus() == AUTHENTICATED) {
            return ResponseEntity.ok(new AuthResponse(result.getAccessToken()));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("다시 접속해주세요.");
        }
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest logoutRequestDto) {
        authenticationService.logout(logoutRequestDto.getEmail());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String token, @RequestBody ChangePasswordRequest request) {
        PasswordChangeResponse response = userService.changeResponse(
                token,
                request.getOldPassword(),
                request.getNewPassword()
        );
        return response.getStatus() == 200
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestHeader("Authorization") String expiredAccessToken) {
        try {
            String newAccessToken = authenticationService.refreshAccessToken(expiredAccessToken);
            return ResponseEntity.ok(RefreshTokenResponse.success(newAccessToken));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(RefreshTokenResponse.fail());
        }
    }
}

