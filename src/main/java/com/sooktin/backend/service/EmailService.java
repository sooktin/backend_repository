package com.sooktin.backend.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final StringRedisTemplate stringRedisTemplate;
    private final AsyncEmailService asyncEmailService;

    // Redis 키 패턴
    private static final String VERIFICATION_KEY_PREFIX = "verification:";
    private static final String RATE_LIMIT_PREFIX = "verification:rate:";
    private static final String ATTEMPT_KEY_PREFIX = "verification_attempts:";

    // 설정값
    private static final int EXPIRATION_MINUTES = 40;
    private static final int MAX_ATTEMPTS = 5;
    private static final int ATTEMPT_RESET_MINUTES = 60;
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(1);

    /**
     * 인증 코드 생성 및 발송 요청 (동기 처리)
     * - 인증 코드 생성/저장: 동기
     * - 이메일 발송: 비동기
     */
    //처음에는 그냥 같이 해도 되는줄알았는데 AOP가 적용이 안되어서 비동기 무시 ㅠㅠ 별도 서비스 분리
    @Transactional
    public VerificationResult sendVerificationCode(String email) {
        try {
            // 1. Rate limiting 체크 (동기)
            String rateLimitKey = RATE_LIMIT_PREFIX + email;
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(rateLimitKey))) {
                log.warn("Rate limit 초과 - 이메일: {}", email);
                return VerificationResult.RATE_LIMITED;
            }

            // 2. 기존 코드 확인 (동기)
            String existingCode = getVerificationCode(email);
            if (existingCode != null) {
                log.info("기존 인증 코드 재발송 - 이메일: {}", email);
                // 기존 코드로 비동기 발송
                asyncEmailService.sendEmailAsync(email, existingCode, "재발송");
                return VerificationResult.SUCCESS;
            }

            // 3. 새 인증 코드 생성 및 Redis 저장 (동기)
            String verificationCode = generateVerificationCode();
            stringRedisTemplate.opsForValue().set(rateLimitKey, "1", RATE_LIMIT_WINDOW);
            String verificationKey = VERIFICATION_KEY_PREFIX + email;
            stringRedisTemplate.opsForValue().set(verificationKey, verificationCode,
                    Duration.ofMinutes(EXPIRATION_MINUTES));

            log.info("인증 코드 생성 및 Redis 저장 완료 - 이메일: {}", email);

            // 4. 비동기 이메일 발송 (트랜잭션 커밋 후 실행)
            asyncEmailService.sendEmailAsync(email, verificationCode, "신규");

            return VerificationResult.SUCCESS;

        } catch (Exception e) {
            log.error("인증 코드 처리 중 오류 - 이메일: {}", email, e);
            return VerificationResult.SYSTEM_ERROR;
        }
    }




    /**
     * 인증 코드 검증 (동기 처리)
     */
    @Transactional
    public VerificationResult verifyCode(String email, String inputCode) {
        try {
            // 시도 횟수 확인
            if (!checkAttemptLimit(email)) {
                log.warn("인증 시도 횟수 초과 - 이메일: {}", email);
                return VerificationResult.TOO_MANY_ATTEMPTS;
            }

            // 인증 코드 조회
            String storedCode = getVerificationCode(email);
            if (storedCode == null) {
                log.warn("인증 코드가 존재하지 않거나 만료됨 - 이메일: {}", email);
                incrementAttemptCount(email);
                return VerificationResult.CODE_NOT_FOUND_OR_EXPIRED;
            }

            if (!storedCode.equals(inputCode)) {
                log.warn("인증 코드 불일치 - 이메일: {}", email);
                incrementAttemptCount(email);
                return VerificationResult.CODE_MISMATCH;
            }

            // 인증 성공 - 코드 삭제 및 시도 횟수 초기화
            deleteVerificationCode(email);
            resetAttemptCount(email);

            log.info("인증 코드 검증 성공 - 이메일: {}", email);
            return VerificationResult.SUCCESS;

        } catch (Exception e) {
            log.error("인증 코드 검증 중 오류 - 이메일: {}", email, e);
            return VerificationResult.SYSTEM_ERROR;
        }
    }
    @Transactional
    public void deleteVerificationCode(String email) {
        String key = VERIFICATION_KEY_PREFIX + email;
        stringRedisTemplate.delete(key);
        log.debug("인증 코드 삭제 완료 - 이메일: {}", email);
    }


    /**
     * 6자리 랜덤 인증 코드 생성
     */
    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    /**
     * 인증 코드 조회
     */
    public String getVerificationCode(String email) {
        String key = VERIFICATION_KEY_PREFIX + email;
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 인증 코드 삭제
     */


    /**
     * 시도 횟수 확인
     */
    private boolean checkAttemptLimit(String email) {
        String key = ATTEMPT_KEY_PREFIX + email;
        String attempts = stringRedisTemplate.opsForValue().get(key);

        if (attempts == null) {
            return true;
        }

        try {
            int attemptCount = Integer.parseInt(attempts);
            return attemptCount < MAX_ATTEMPTS;
        } catch (NumberFormatException e) {
            log.warn("시도 횟수 파싱 오류 - 이메일: {}, 값: {}", email, attempts);
            return true;
        }
    }

    /**
     * 시도 횟수 증가
     */
    private void incrementAttemptCount(String email) {
        String key = ATTEMPT_KEY_PREFIX + email;
        String attempts = stringRedisTemplate.opsForValue().get(key);

        int currentAttempts = 0;
        if (attempts != null) {
            try {
                currentAttempts = Integer.parseInt(attempts);
            } catch (NumberFormatException e) {
                log.warn("시도 횟수 파싱 오류 - 이메일: {}, 값: {}", email, attempts);
            }
        }

        currentAttempts++;
        stringRedisTemplate.opsForValue().set(key, String.valueOf(currentAttempts),
                Duration.ofMinutes(ATTEMPT_RESET_MINUTES));

        log.debug("인증 시도 횟수 증가 - 이메일: {}, 횟수: {}/{}", email, currentAttempts, MAX_ATTEMPTS);
    }

    /**
     * 시도 횟수 초기화
     */
    private void resetAttemptCount(String email) {
        String key = ATTEMPT_KEY_PREFIX + email;
        stringRedisTemplate.delete(key);
        log.debug("인증 시도 횟수 초기화 - 이메일: {}", email);
    }

    /**
     * 인증 결과 열거형
     */
    public enum VerificationResult {
        SUCCESS("성공"),
        RATE_LIMITED("1분에 한 번만 요청 가능합니다"),
        CODE_NOT_FOUND_OR_EXPIRED("인증 코드가 없거나 만료되었습니다"),
        CODE_MISMATCH("인증 코드가 일치하지 않습니다"),
        TOO_MANY_ATTEMPTS("인증 시도 횟수를 초과했습니다"),
        SYSTEM_ERROR("시스템 오류가 발생했습니다");

        private final String message;

        VerificationResult(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}