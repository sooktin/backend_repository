package com.sooktin.backend.service;

import com.sooktin.backend.domain.User;
import com.sooktin.backend.domain.UserRole;
import com.sooktin.backend.domain.VerificationToken;
import com.sooktin.backend.repository.UserRepository;
import com.sooktin.backend.repository.VerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Collections;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Transactional
    public User registerUser(User user) throws Exception {
        if (userRepository.existsByNickname(user.getNickname())) {
            throw new Exception("닉네임이 이미 존재합니다.");
        }
        if (userRepository.existsByEmail(user.getEmail())){
            throw new Exception("이메일이 이미 존재합니다.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Collections.singleton(UserRole.USER));
        //user.setEmail(false);

        User savedUser = userRepository.save(user);

        String token = UUID.randomUUID().toString();
        createVerificationToken(savedUser,token);

        emailService.sendVerificationEmail(user.getEmail(),token);

        return savedUser;
    }

    @Transactional
    public void createVerificationToken(User user, String token) {
        VerificationToken myToken = new VerificationToken(token, user);
        tokenRepository.save(myToken);
    }

    @Transactional
    public String confirmEmail(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token);
        if (verificationToken == null){
            return "Invalid token";
        }
        User user = verificationToken.getUser();
        Calendar calendar = Calendar.getInstance();
        if (verificationToken.getExpiryDate().getTime() - calendar.getTime().getTime() <= 0){
            return "Token expired";
        }
        //user.setEmail(true);
        userRepository.save(user);
        return "이메일 인증에 성공하였습니다.";
    }
}
