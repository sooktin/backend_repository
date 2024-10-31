package com.sooktin.backend.controller;

import com.sooktin.backend.auth.AuthResponse;
import com.sooktin.backend.auth.AuthenticationResult;
import com.sooktin.backend.auth.AuthenticationStatus;
import com.sooktin.backend.auth.JwtUtil;
import com.sooktin.backend.dto.TwofaRequiredResponse;
import com.sooktin.backend.domain.User;

import com.sooktin.backend.dto.email.EmailCheckRequest;
import com.sooktin.backend.dto.email.EmailCheckResponse;

import com.sooktin.backend.dto.user.ChangePasswordRequest;
import com.sooktin.backend.dto.user.LogoutRequestDto;
import com.sooktin.backend.dto.user.PasswordChangeResponse;
import com.sooktin.backend.repository.UserRepository;
import com.sooktin.backend.service.AuthenticationService;
import com.sooktin.backend.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import static com.sooktin.backend.auth.AuthenticationStatus.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {
    private final AuthenticationService authenticationService;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Operation(summary = "회원 가입 API", description = "회원 가입에 성공하면 이메일 인증(2fa)을 거칩니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "2fa로 넘어갑니다. 추후 201 코드로 바뀔듯요",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "회원가입에 성공하셨습니다. 이메일 인증을 위하여 이메일함을 확인해주세요."))

            ),
            @ApiResponse(
                    responseCode = "400", description = "잘못된 요청.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "이미 존재하는 이메일입니다."))
            )
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "사용자 회원 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = User.class))
            )
            @RequestBody User user
    ) {
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
                            schema = @Schema(implementation = EmailCheckResponse.class),
                            examples = @ExampleObject(value = "이메일이 확인되었습니다. 비밀번호를 입력해주세요. ")
                    )}
            ),
            @ApiResponse(responseCode = "404", description = "이메일 없음 ㅠㅠ",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = EmailCheckResponse.class),
                            examples = @ExampleObject(value = "이메일이 존재하지 않아 회원가입으로 이동합니다.")
                    )}
            )
    })

    @PostMapping("/check-email")
    public ResponseEntity<EmailCheckResponse> checkEmail(@RequestBody EmailCheckRequest request) {
        AuthenticationResult authenticationResult = authenticationService.checkEmailExists(request.getEmail());
        if (authenticationResult.getStatus() == AUTHENTICATED) {
            return ResponseEntity.ok(new EmailCheckResponse(true, "이메일이 확인되었습니다. 비밀번호를 입력해주세요."));
        } else if (authenticationResult.getStatus() == ACCOUNT_DISABLED) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new EmailCheckResponse(true, "계정이 정지되었습니다. 고객센터에 문의해주세요."));
        } else if (authenticationResult.getStatus() == NONE_ACCOUNT) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new EmailCheckResponse(false, "이메일이 존재하지 않아 회원가입으로 이동합니다."));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EmailCheckResponse(false, "내부 서버 오류입니다. 다시접속해주세요."));
        }
    }

    @Operation(summary = "로그인 API", description = "2차인증이 필요하다면 2차인증 수속을 다시 밟고 이미 되었다면 JWT 토큰을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공 또는 2FA 필요",
                    content = {@Content(schema = @Schema(oneOf = {TwofaRequiredResponse.class, AuthResponse.class}))}),
            @ApiResponse(responseCode = "500", description = "서버 내 오류", content = {@Content(mediaType = "applicatoin/json",
                    examples = @ExampleObject(value = "다시 접속해주세요."))})
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "사용자 로그인 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = User.class))
            )
            //TODO 유저 로그인 리퀘스트Dto로 바꾸기
            @RequestBody User user) {
        AuthenticationResult result = authenticationService.authenticate(user.getEmail(), user.getPassword());
        if (result.getStatus() == AuthenticationStatus.REQUIRES_2FA) {
            return ResponseEntity.ok(new TwofaRequiredResponse());
        } else if (result.getStatus() == AUTHENTICATED) {
            return ResponseEntity.ok(new AuthResponse(result.getToken()));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("다시 접속해주세요.");
        }
    }


    @Operation(summary = "로그아웃 API", description = "이미 로그아웃되었으므로 아무것도 반환하지 않습니다.")
    @ApiResponse(responseCode = "200", description = "")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "사용자 로그아웃 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LogoutRequestDto.class))
            )
            @RequestBody LogoutRequestDto logoutRequestDto
    ) {
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

}

