package com.sooktin.backend.service;


import com.sooktin.backend.auth.AuthenticationResult;
import com.sooktin.backend.auth.AuthenticationStatus;
import com.sooktin.backend.auth.JwtUtil;
import com.sooktin.backend.domain.User;
import com.sooktin.backend.global.exception.InvalidTwoFaCodeException;
import com.sooktin.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

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
            return new AuthenticationResult(AuthenticationStatus.ACCOUNT_DISABLED, null);
        }
        if (authentication.getName() == null) {
            return new AuthenticationResult(AuthenticationStatus.NONE_ACCOUNT, null);
        }

        if (userDetails.is2faEnabled()) {
            String twoFc = generate2faCode();
            redisTemplate.opsForValue().set(
                    "JWT_" + userDetails.getUsername(),
                    twoFc,
                    Duration.ofMinutes(5)
            );
            emailService.sendVerificationEmail(userDetails.getUsername(), twoFc);
            return new AuthenticationResult(AuthenticationStatus.REQUIRES_2FA, null);
        } else {
            String token = generateTokenAndSave(userDetails);
            return new AuthenticationResult(AuthenticationStatus.AUTHENTICATED, token);
        }


    }


    public String completeTFAuthentication(String email, String twoFc) {

        String storedCode = redisTemplate.opsForValue().get("2FA_" + email);
        if (storedCode == null) {
            throw new InvalidTwoFaCodeException("2FA code has expired or does not exist");
        }
        if (!storedCode.equals(twoFc)) {
            throw new InvalidTwoFaCodeException("Invalid 2FA code");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        CustomUserDetails userDetails = new CustomUserDetails(user);
        redisTemplate.delete("JWT_" + email);
        return generateTokenAndSave(userDetails);
    }

    private String generate2faCode() {
        Random random = new Random();
        int num = random.nextInt(999999);

        return String.format("%06d", num);
    }


    private String generateTokenAndSave(CustomUserDetails userDetails) {
        String token = jwtUtil.generateToken(userDetails);
        redisTemplate.opsForValue().set(
                "JWT_" + userDetails.getUsername(),
                token,
                Duration.ofMillis(jwtUtil.getExpiration())
        );
        return token;
    }


    public void logout(String email) {
        try {
            SecurityContextHolder.clearContext(); //보안 컨텍스트 정리
            redisTemplate.delete("JWT_" + email);
        } catch (Exception e) {
            throw new RuntimeException("logout failed", e);
        }
    }

    public boolean validateToken(String token) {
        String email = jwtUtil.getEmailFromToken(token);
        String storedToken = redisTemplate.opsForValue().get("JWT_" + email);
        return token.equals(storedToken);
    }


    public AuthenticationResult checkEmailExists(String email) {
        try {
            CustomUserDetails userDetails =
                    (CustomUserDetails) customUserDetailsService.loadUserByUsername(email);

            if (userDetails.isEmailNull()) {
                return new AuthenticationResult(AuthenticationStatus.NONE_ACCOUNT, null);
            } else {
                return new AuthenticationResult(AuthenticationStatus.AUTHENTICATED, null);
            }
        } catch (UsernameNotFoundException e) {
            return new AuthenticationResult(AuthenticationStatus.NONE_ACCOUNT, null);
        }

    }


    public String extractUsername(String token) {

        return jwtUtil.extractUsername(token);
    }

    public UserDetails loadUserByUsername(String username) {
        return customUserDetailsService.loadUserByUsername(username);
    }

}