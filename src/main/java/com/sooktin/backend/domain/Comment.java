package com.sooktin.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 회원ID 외래키 - 다대일
    @JoinColumn(name = "user_id", nullable = false) // 외래키 설정
    private User user; // 행위자

    private Integer likes; // 좋아요

    @ManyToOne(fetch = FetchType.LAZY) // 노트ID 외래키 - 다대일
    @JoinColumn(name = "usernote_id", nullable = false) // 외래키 설정
    private Usernote usernote;

    @Column(name = "parent_id", nullable = true)
    private Long parentId; // 부모 댓글 ID

    @OneToMany(mappedBy = "parentId", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Comment> replies; // 대댓글 목록

    @Column(nullable = false, length = 300)
    private String content;

    @Column(nullable = false)
    private boolean deleted = false; // 댓글 삭제 여부

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime modifiedAt;

    public void markAsDeleted() {
        this.deleted = true;
        this.content = "삭제된 댓글입니다.";
    }
}
