package com.sooktin.backend.dto.comment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sooktin.backend.domain.Comment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCommentResponse {
    private Long id;
    private String content;
    private Integer likes;
    private Long userId;
    private Long usernoteId;
    private Boolean isParent; // 부모 댓글 여부

    @JsonInclude(JsonInclude.Include.NON_NULL) // null인 경우 응답에서 제외
    private Long parentId; // 부모 댓글 ID (대댓글일 경우만 표시)

    public CreateCommentResponse(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.likes = comment.getLikes();
        this.userId = comment.getUser().getId();
        this.usernoteId = comment.getUsernote().getId();
        this.isParent = comment.getParentId() == null; // parentId가 null이면 부모 댓글
        this.parentId = this.isParent ? null : comment.getParentId(); // 부모 댓글일 경우 null
    }
}