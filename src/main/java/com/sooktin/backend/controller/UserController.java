package com.sooktin.backend.controller;

import com.nimbusds.openid.connect.sdk.LogoutRequest;
import com.sooktin.backend.auth.AuthResponse;
import com.sooktin.backend.auth.AuthenticationResult;
import com.sooktin.backend.auth.AuthenticationStatus;
import com.sooktin.backend.auth.TwofaRequiredResponse;
import com.sooktin.backend.domain.User;
import com.sooktin.backend.dto.user.UserDto;
import com.sooktin.backend.service.AuthenticationService;
import com.sooktin.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {
    private final AuthenticationService authenticationService;

    private final UserService userService;

    @GetMapping("/register")
    public String showRegister(){
        return "good";
    }
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user){
        try {
            User registeredUser = userService.registerUser(user);
            return ResponseEntity.ok("회원가입에 성공하셨습니다. 이메일 인증을 위하여 이메일함을 확인해주세요. ");
        } catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/2fa")
    public ResponseEntity<String> confirmEmail(@RequestParam("token") String token) {
        try {
            String result = userService.confirmEmail(token);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("에러 발생");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user){
        AuthenticationResult result = authenticationService.authenticate(user.getEmail(), user.getPassword());
        if (result.getStatus() == AuthenticationStatus.REQUIRES_2FA) {
            return ResponseEntity.ok(new TwofaRequiredResponse());
        } else if (result.getStatus() == AuthenticationStatus.AUTHENTICATED) {
            return ResponseEntity.ok(new AuthResponse(result.getToken()));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("다시 접속해주세요.");
        }

    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody UserDto.LogoutRequestDto logoutRequestDto){
        authenticationService.logout(logoutRequestDto.getEmail());
        return ResponseEntity.ok().build();
    }
}
