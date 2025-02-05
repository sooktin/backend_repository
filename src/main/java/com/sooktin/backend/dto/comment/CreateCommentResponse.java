package com.sooktin.backend.dto.comment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sooktin.backend.domain.Comment;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateCommentResponse {
    private Long commentId; // 댓글 ID
    private Long userId; // 작성자 ID
    private Long usernoteId; // 댓글이 속한 게시글 ID
    private String nickname; // 작성자 닉네임
    private String content; // 댓글 내용

    private Boolean isParent; // 부모 댓글 여부
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long parentId; // 부모 댓글 ID (대댓글일 경우)

    //private Integer likes; // 좋아요 수

    private LocalDateTime createdAt; // 생성 일시
    private LocalDateTime modifiedAt; // 수정 일시

    @JsonIgnore
    private boolean isDeleted; // 삭제 여부

    public CreateCommentResponse(Comment comment) {
        this.commentId = comment.getId();
        this.userId = comment.getUser().getId();
        this.usernoteId = comment.getUsernote().getId();
        this.nickname = comment.getUser().getNickname();
        this.content = comment.isDeleted() ? "삭제된 댓글입니다." : comment.getContent();
        this.isParent = comment.getParentId() == null; // parentId가 null이면 부모 댓글
        this.parentId = this.isParent ? null : comment.getParentId(); // 부모 댓글일 경우 null
        //this.likes = comment.getLikes();
        this.createdAt = comment.getCreatedAt();
        this.modifiedAt = comment.getModifiedAt();
    }
}