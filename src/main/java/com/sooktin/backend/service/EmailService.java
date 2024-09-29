package com.sooktin.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String token) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("숙틴에 오신 걸 환영합니다.");
        msg.setText("다음을 클릭해서 인증에 성공하세요!\n"
                + "http://localhost:8080/auth/2fa?token=" + token);
        mailSender.send(msg);
    }
}
