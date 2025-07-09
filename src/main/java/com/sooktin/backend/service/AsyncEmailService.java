package com.sooktin.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncEmailService {

    private final JavaMailSender mailSender;
    private final StringRedisTemplate stringRedisTemplate;
    // Redis 키 패턴 추가
    private static final String VERIFICATION_KEY_PREFIX = "verification:";


    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendEmailAsync(String email, String code, String type) {
        try {
            log.info("비동기 이메일 발송 시작 - 이메일: {}, 타입: {}, 스레드: {}",
                    email, type, Thread.currentThread().getName());

            SimpleMailMessage msg = createEmailMessage(email, code, type);
            mailSender.send(msg);

            log.info("비동기 이메일 발송 완료 - 이메일: {}, 타입: {}", email, type);

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("비동기 이메일 발송 실패 - 이메일: {}, 타입: {}, 오류: {}",
                    email, type, e.getMessage(), e);

            // 발송 실패 시 인증 코드 삭제 (별도 트랜잭션에서)
            CompletableFuture.runAsync(() -> {
                try {
                    deleteVerificationCode(email);
                    log.info("발송 실패로 인한 인증 코드 삭제 완료 - 이메일: {}", email);
                } catch (Exception deleteEx) {
                    log.error("인증 코드 삭제 실패 - 이메일: {}", email, deleteEx);
                }
            });

            return CompletableFuture.failedFuture(e);
        }
    }

    private SimpleMailMessage createEmailMessage(String email, String code, String type) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);

        if ("재발송".equals(type)) {
            msg.setSubject("숙틴 인증 코드 재발송");
            msg.setText("요청하신 인증 코드를 재발송합니다.\n" + code +
                    "\n이 코드는 40분 동안 유효합니다.");
        } else {
            msg.setSubject("숙틴에 오신 걸 환영합니다.");
            msg.setText("다음을 인증 코드를 전 페이지로 돌아가 입력해주세요!\n" + code +
                    "\n이 코드는 40분 동안 유효합니다.");
        }

        return msg;
    }

    /**
     * 인증 코드 삭제
     */
    @Transactional
    public void deleteVerificationCode(String email) {
        String key = VERIFICATION_KEY_PREFIX + email;
        stringRedisTemplate.delete(key);
        log.debug("인증 코드 삭제 완료 - 이메일: {}", email);
    }
}