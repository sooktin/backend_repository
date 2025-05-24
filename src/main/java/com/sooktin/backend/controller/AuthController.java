package com.sooktin.backend.controller;

import com.sooktin.backend.dto.ResponseDto;
import com.sooktin.backend.dto.user.AuthResponse;
import com.sooktin.backend.auth.AuthenticationResult;
import com.sooktin.backend.auth.JwtUtil;

import com.sooktin.backend.dto.email.EmailCheckRequest;
import com.sooktin.backend.dto.email.EmailCheckResponse;

import com.sooktin.backend.dto.user.*;
import com.sooktin.backend.dto.verification.VerficationResponse;
import com.sooktin.backend.dto.verification.VerificationRequest;
import com.sooktin.backend.global.exception.auth.DuplicateEmailException;
import com.sooktin.backend.global.exception.auth.DuplicateNicknameException;
import com.sooktin.backend.global.exception.auth.DuplicateResourceException;
import com.sooktin.backend.global.exception.auth.PasswordMismatchException;
import com.sooktin.backend.service.AuthenticationService;
import com.sooktin.backend.service.CustomUserDetails;
import com.sooktin.backend.service.EmailService;
import com.sooktin.backend.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.sooktin.backend.auth.AuthenticationStatus.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final EmailService emailService;

    @GetMapping("/id")
    public String id() {
        return "hey 나연";
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerUser(@RequestBody @Valid RegisterRequest request) {
        try {
            userService.registerUser(request);
            return ResponseEntity.status(201).body(RegisterResponse.success());
        } catch (PasswordMismatchException e) {
            return ResponseEntity.badRequest().body(RegisterResponse.passwordMismatch());
        } catch (DuplicateNicknameException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(RegisterResponse.duplicateNickname());
        } catch (DuplicateEmailException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(RegisterResponse.duplicateEmail());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<VerficationResponse> verifyEmail(@RequestBody VerificationRequest request) {
        VerficationResponse response = userService.verifyEmail(request.getEmail(), request.getToken());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/send-verification")
    public ResponseEntity<ResponseDto<Object>> sendVerificationCode(@RequestBody SendVerificationRequest request) {

            log.info("인증 코드 발송 요청 - 이메일: {}", request.getEmail());

            // EmailService에서 처리 (동기: 코드 생성/저장, 비동기: 이메일 발송)
            EmailService.VerificationResult result = emailService.sendVerificationCode(request.getEmail());

            return switch (result) {
                case SUCCESS -> {
                    log.info("인증 코드 생성 및 발송 시작 - 이메일: {}", request.getEmail());
                    yield ResponseEntity.ok(new ResponseDto<>(
                            200,
                            "인증 코드가 생성되었습니다. 이메일을 확인해주세요.",
                            null
                    ));
                }
                case RATE_LIMITED -> {
                    log.warn("Rate limit 초과 - 이메일: {}", request.getEmail());
                    yield ResponseEntity.status(429).body(new ResponseDto<>(
                            429,
                            result.getMessage(),
                            null
                    ));
                }
                default -> {
                    log.error("인증 코드 처리 실패 - 이메일: {}, 결과: {}", request.getEmail(), result);
                    yield ResponseEntity.status(500).body(new ResponseDto<>(
                            500,
                            result.getMessage(),
                            null
                    ));
                }
            };

    }

    @PostMapping("/check-email")
    public ResponseEntity<EmailCheckResponse> checkEmail(@RequestBody @Valid EmailCheckRequest request) {
        EmailCheckResponse response = userService.checkEmail(request.getEmail());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        AuthenticationResult result = authenticationService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());

        LoginResponse response =  result.getStatus() == AUTHENTICATED
                ? LoginResponse.success(result.getAccessToken())
                : LoginResponse.fail();

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("로그아웃 실패: 인증 정보 없음");
        }

        String email = authentication.getName(); // 현재 인증된 사용자의 이메일

        authenticationService.logout(email);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("password")
    public ResponseEntity<PasswordChangeResponse> changePassword(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody @Valid PasswordChangeRequest request) {
        PasswordChangeResponse response = userService.changePassword(
                userDetails.getUserId(),
                request.getOldPassword(),
                request.getNewPassword()
        );
        return response.getStatusCode() == 200
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @DeleteMapping("/verification/{email}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseDto<Object>> deleteVerification(@PathVariable String email) {
        try {
            userService.removeVerificationToken(email);
            log.info("관리자에 의한 인증 코드 삭제 - 이메일: {}", email);
            return ResponseEntity.ok(new ResponseDto<>(
                    200,
                    "인증 코드가 삭제되었습니다.",
                    null
            ));
        } catch (Exception e) {
            log.error("인증 코드 삭제 실패 - 이메일: {}", email, e);
            return ResponseEntity.status(500).body(new ResponseDto<>(
                    500,
                    "인증 코드 삭제에 실패했습니다.",
                    null
            ));
        }
    }
}

