package com.sooktin.backend.dto.usernote;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateUserNoteRequestDto {
    private Long userId;
    private String title;
    private String content;
    private Integer likes;
}
