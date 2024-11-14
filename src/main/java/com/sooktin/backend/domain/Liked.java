package com.sooktin.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "Liked")
public class Liked {

    //좋아요 id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "좋아요id", nullable = false)
    private Long liked_id;

    //댓글 id
    @Column(name = "댓글 id",length = 25, nullable = false)
    private Long comment_id;

    //노트 id
    @ManyToOne(fetch = FetchType.LAZY) // 게시물 외래키 - 다대일
    @JoinColumn(name = "post_id", nullable = false)
    private Usernote post_id; // Usernote 엔티티와 연결

    //좋아요 생성일시
    @CreationTimestamp
    @Column(name = "생성일자", nullable = false)
    private LocalDateTime created_at; // 생성일시


    @ManyToOne(fetch = FetchType.LAZY) // 사용자 외래키 - 다대일
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // User 엔티티와 연결

    @Column(name = "회원ID",length = 300, nullable = false)
    private Long userId; // 내용

}