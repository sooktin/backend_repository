package com.sooktin.backend.service;


import com.sooktin.backend.auth.AuthenticationResult;
import com.sooktin.backend.auth.AuthenticationStatus;
import com.sooktin.backend.auth.JwtUtil;
import com.sooktin.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final CustomUserDetailsService customUserDetailsService;

    public AuthenticationResult authenticate(String email, String password) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        if (!userDetails.isEnabled()) {
            return new AuthenticationResult(AuthenticationStatus.ACCOUNT_DISABLED,null, null);
        }
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        redisTemplate.opsForValue().set(
                "REFRESH_" + userDetails.getUsername(),
                refreshToken,
                Duration.ofMillis(jwtUtil.getRefresh_expiration())
        );
        return new AuthenticationResult(AuthenticationStatus.AUTHENTICATED,"로그인 되었습니다.", accessToken);
    }

    public String refreshAccessToken(String expiredAccessToken) {
        String email = jwtUtil.getEmailFromToken(expiredAccessToken);
        String refreshToken = redisTemplate.opsForValue().get("REFRESH_" + email);
        if (refreshToken != null && jwtUtil.validateToken(refreshToken)) {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
            return jwtUtil.generateAccessToken((CustomUserDetails) userDetails);
        }
        throw new RuntimeException("리프레시 토큰이 레디스에 없습니다.");
    }

    public void logout(String email) {
        if (email == null) {
            throw new RuntimeException("로그아웃 실패: 사용자 정보 없음");
        }

        SecurityContextHolder.clearContext(); // 보안 컨텍스트 초기화
        redisTemplate.delete("REFRESH_" + email);
    }

    public Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUserId();
        }
        throw new RuntimeException("로그인된 사용자가 없습니다.");
    }

}