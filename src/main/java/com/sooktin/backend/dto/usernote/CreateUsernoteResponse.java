package com.sooktin.backend.dto.usernote;

import com.sooktin.backend.domain.Usernote;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUsernoteResponse {
    private Long id;            // 게시글 ID
    private String title;       // 제목
    private String content;     // 내용
    private Integer likes;      // 좋아요 수
    private String createdAt;   // 생성일
    private String modifiedAt;  // 수정일
    private Long userId;        // 작성자 ID
    private String userEmail;   // 작성자 이메일
    private String userNickname; // 작성자 닉네임

    public CreateUsernoteResponse(Usernote usernote) {
        this.id = usernote.getId();
        this.title = usernote.getTitle();
        this.content = usernote.getContent();
        this.likes = usernote.getLikes();
        this.createdAt = usernote.getCreated_at().toString();
        this.modifiedAt = usernote.getModified_at().toString();
        this.userId = usernote.getUser().getId();
        this.userEmail = usernote.getUser().getEmail();
        this.userNickname = usernote.getUser().getNickname();
    }
}