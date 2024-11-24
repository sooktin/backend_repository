package com.sooktin.backend.controller;

import com.sooktin.backend.auth.JwtUtil;
import com.sooktin.backend.domain.User;
import com.sooktin.backend.dto.user.deleteUserRequest;
import com.sooktin.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    //delete되는지 가라 기능 작업 수행임
    @GetMapping("/search")
    public ResponseEntity<Optional<User>> searchUsers(@RequestParam String nickname) {
        return ResponseEntity.ok((userService.search(nickname)));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUser(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
            }

            String userEmail = jwtUtil.getEmailFromToken(token);;
            userService.delete(userEmail);
            return ResponseEntity.ok().body("회원탈퇴성공");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
