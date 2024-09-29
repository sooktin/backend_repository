package com.sooktin.backend.service;


import com.sooktin.backend.auth.AuthenticationResult;
import com.sooktin.backend.auth.AuthenticationStatus;
import com.sooktin.backend.auth.JwtUtil;
import com.sooktin.backend.domain.User;
import com.sooktin.backend.global.InvalidTwoFaCodeException;
import com.sooktin.backend.repository.UserRepository;
import com.sooktin.backend.service.CustomUserDetails;
import com.sooktin.backend.service.EmailService;
import com.sooktin.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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


    public AuthenticationResult authenticate(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        if (userDetails.is2faEnabled()) {
            String twoFc = generate2faCode();
            redisTemplate.opsForValue().set(
                    "2FA_" + userDetails.getUsername(),
                    twoFc,
                    Duration.ofMinutes(5)
            );
            emailService.sendVerificationEmail(userDetails.getUsername(), twoFc);
            return new AuthenticationResult(AuthenticationStatus.REQUIRES_2FA, null);
        } else {
            String token = generateTokenAndSave(userDetails);
            return new AuthenticationResult(AuthenticationStatus.AUTHENTICATED,token);
        }
    }

    public String completeTFAuthentication(String email, String twoFc){
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
        redisTemplate.delete("2FA_" + email);
        return generateTokenAndSave(userDetails);
    }

    private String generate2faCode() {
        Random random = new Random();
        int num = random.nextInt(999999);
        return String.format("%06d",num);
    }

    private String generateTokenAndSave (CustomUserDetails userDetails){
        String token = jwtUtil.generateToken(userDetails);
        redisTemplate.opsForValue().set(
                "JWT_" + userDetails.getUsername(),
                token,
                Duration.ofMillis(jwtUtil.getExpiration())
        );
        return token;
    }

    public void logout(String email){
        redisTemplate.delete("JWT_"+email);
    }
    public boolean validateToken(String token) {
        /*if (!jwtUtil.validateToken(token)) {
            return false;
        }*/
        String email = jwtUtil.getEmailFromToken(token);
        String storedToken = redisTemplate.opsForValue().get("JWT_" + email);
        return token.equals(storedToken);
    }
}