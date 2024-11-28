package com.sooktin.backend.controller;

import com.sooktin.backend.auth.JwtUtil;
import com.sooktin.backend.domain.User;
import com.sooktin.backend.domain.Usernote;
import com.sooktin.backend.dto.user.deleteUserRequest;
import com.sooktin.backend.service.UserService;
import com.sooktin.backend.service.UsernoteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UsernoteService usernoteService;

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
    @GetMapping("/usernotes")
    public ResponseEntity<?> getUserNotes(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(usernoteService.findByUserEmail(userDetails.getUsername()));
    }


}
