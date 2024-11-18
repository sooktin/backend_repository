package com.sooktin.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String code) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("숙틴에 오신 걸 환영합니다.");
        msg.setText("다음을 인증 코드를 전 페이지로 돌아가 입력해주세요!\n"
                + code + "\n" + "이 코드는 40분 동안 유효합니다.");
        mailSender.send(msg);
    }
}
