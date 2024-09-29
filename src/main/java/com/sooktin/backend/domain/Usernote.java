package com.sooktin.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "usernotes")
public class Usernote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id; // 노트id

    @Column(length = 25, nullable = false)
    private String title; // 제목

    @Column(length = 300, nullable = false)
    private String content; // 내용

    private Integer likes; // 좋아요

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime created_at; // 생성일시

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime modified_at; // 수정일시

    // User와 일대일 관계 설정 (회원ID 외래키)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // 외래키 선언
    private User user; // 회원 정보 (User 엔티티와 연결)
}