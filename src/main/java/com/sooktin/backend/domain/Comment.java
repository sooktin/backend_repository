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

    @ManyToOne(fetch = FetchType.LAZY) // 노트ID 외래키 - 다대일
    @JoinColumn(name = "usernote_id", nullable = false) // 외래키 설정
    private Usernote usernote;

    @ManyToOne(fetch = FetchType.LAZY) // 부모 댓글 - 다대일
    @JoinColumn(name = "parent_id") // 대댓글을 위한 부모 댓글
    private Comment parent; // 부모 댓글 - 해당 변수가 null이 아니면 대댓글로 취급

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL) // 자식 댓글
    private List<Comment> replies; // 대댓글 목록

    @Column(nullable = false, length = 300)
    private String content;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime modifiedAt;
}
