package com.sooktin.backend.service;

import com.sooktin.backend.auth.JwtUtil;
import com.sooktin.backend.domain.CareerCardStorage;
import com.sooktin.backend.domain.User;
import com.sooktin.backend.domain.UserRole;
import com.sooktin.backend.domain.VerificationToken;
import com.sooktin.backend.dto.email.EmailCheckResponse;
import com.sooktin.backend.dto.user.NicknameResponse;
import com.sooktin.backend.dto.user.PasswordChangeResponse;
import com.sooktin.backend.dto.user.RegisterRequest;
import com.sooktin.backend.dto.verification.VerficationResponse;
import com.sooktin.backend.global.exception.auth.DuplicateEmailException;
import com.sooktin.backend.global.exception.auth.DuplicateNicknameException;
import com.sooktin.backend.global.exception.auth.PasswordMismatchException;
import com.sooktin.backend.repository.StorageRepository;
import com.sooktin.backend.repository.UserRepository;
import com.sooktin.backend.repository.VerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final VerificationRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    @Qualifier("redisTemplate")
    private final RedisTemplate redisTemplate;

    @Transactional
    public void registerUser(RegisterRequest request) {

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("비밀번호가 일치하지 않습니다.");
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new DuplicateEmailException();
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateNicknameException();
        }

        User newUser = User.builder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Collections.singleton(UserRole.USER))
                .build();
        CareerCardStorage storage = CareerCardStorage.builder()
                .user(newUser)
                .build();

        newUser.setCareerCardStorage(storage);

        userRepository.save(newUser);
    }

    @Transactional
    public PasswordChangeResponse changePassword(Long userId, String oldPassword, String newPassword) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("없는 회원입니다!"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return new PasswordChangeResponse(400, "비밀번호를 제대로 입력해주세요",null);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return new PasswordChangeResponse(200, "비밀번호가 변경되었습니다",null);
    }

    @Transactional
    public NicknameResponse changeNickname(String currentNickname, String newNickname) {
        User user = userRepository.findByNickname(currentNickname)
                .orElseThrow(() -> new UsernameNotFoundException("없는 회원입니다"));

        if (user.getNickname().equals(newNickname)){
            throw new IllegalArgumentException("중복 닉네임입니다.");
        }
        if (userRepository.existsByNickname(newNickname)) {
            throw new IllegalArgumentException("다른 닉네임을 입력해주세요");
        }
        user.setNickname(newNickname);
        userRepository.save(user);

        return new NicknameResponse(200,"닉네임이 변경되었습니다.", user);
    }

    @Transactional(readOnly = true)
    public EmailCheckResponse checkEmail(String email) {

            if (userRepository.existsByEmail(email)) {
                return EmailCheckResponse.loginRequired();
            }
            return EmailCheckResponse.registerRequired();


    }

    @Transactional
    public void createVerificationToken(String email, String code) {
        VerificationToken verificationToken = new VerificationToken(code, email);
        tokenRepository.save(verificationToken);
        emailService.sendVerificationEmail(email, code);
    }

    @Transactional
    public void sendVerificationCode(String email) {
        String verficationCode = generateVerificationCode();

        createVerificationToken(email, verficationCode);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    @Transactional(readOnly = true)
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public VerficationResponse verifyEmail(String email, String code) {
        VerificationToken verificationToken = tokenRepository.findByEmailAndToken(email,code);

        if (verificationToken == null) {
            return VerficationResponse.invalidCode();
        }

        Calendar calendar = Calendar.getInstance();
        if (verificationToken.getExpiryDate().getTime() - calendar.getTime().getTime() <= 0) {
            tokenRepository.delete(verificationToken);
            return VerficationResponse.expired();
        }

        tokenRepository.delete(verificationToken);

        return VerficationResponse.success();
    }

    @Transactional(readOnly = true)
    public Optional<User> search(String nickname) {
        return userRepository.findByNickname(nickname);
    }

    @Transactional(readOnly = true)
    public void delete(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // 연관된 데이터 처리 (예: 게시글, 댓글 등)
        // postRepository.deleteByUser(user);
        // commentRepository.deleteByUser(user);

        userRepository.delete(user);

        String refreshtoken = "REFRESH_" + user.getEmail();
        redisTemplate.delete(refreshtoken);
    }
}
