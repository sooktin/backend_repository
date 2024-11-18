package com.sooktin.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

@Entity
@Getter
public class VerificationToken {
    private static final int EXPIRATION = 40; // 40 mins!

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;
    private String email;

    private Date expiryDate;

    public VerificationToken() {
        this.expiryDate = calculateExpiryDate(EXPIRATION);
    }
    public VerificationToken(String token,String email){
        this.email = email;
        this.token = token;
        this.expiryDate = calculateExpiryDate(EXPIRATION);
    }
    private Date calculateExpiryDate(int expiryTimeInMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Timestamp(calendar.getTime().getTime()));
        calendar.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(calendar.getTime().getTime());
    }
}
