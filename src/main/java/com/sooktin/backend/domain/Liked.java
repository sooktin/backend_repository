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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long liked_id;

    // 댓글 ID (nullable: 좋아요가 게시물에 연결된 경우 null 가능)
    //좋아요가 게시물에 속하는지 댓글에 속하는지 명확히 구분해야 하기 때문임
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment id", nullable = true)
    private Comment comment;

    //노트 id
    @ManyToOne(fetch = FetchType.LAZY) // 게시물 외래키 - 다대일
    @JoinColumn(name = "post_id", nullable = true)
    private Usernote post; // Usernote 엔티티와 연결

    @ManyToOne(fetch = FetchType.LAZY) // 사용자 외래키 - 다대일
    @JoinColumn(name = "user_id")
    private User user; // User 엔티티와 연결

    //좋아요 생성일시
    @CreationTimestamp
    @Column(name = "생성일자", nullable = false)
    private LocalDateTime created_at; // 생성일시

}