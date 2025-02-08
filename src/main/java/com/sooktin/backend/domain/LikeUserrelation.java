package com.sooktin.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "LikeUserrelation")
public class LikeUserrelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "좋아요id", nullable = false)
    private Long liked_id; // 노트id

    @Column(name = "회원ID",length = 300, nullable = false)
    private Long userId; // 내용

    // Usernote와 일대다 관계 설정 (회원ID 외래키)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // 외래키 선언
    private User user; // 회원 정보 (User 엔티티와 연결)

    //

}