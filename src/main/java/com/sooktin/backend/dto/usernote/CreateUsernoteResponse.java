package com.sooktin.backend.dto.usernote;

import com.sooktin.backend.domain.Usernote;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUsernoteResponse {
    private Long id;
    private String title;
    private String content;

    public CreateUsernoteResponse(Usernote usernote) {
        this.id = usernote.getId();
        this.title = usernote.getTitle();
        this.content = usernote.getContent();
    }
}
