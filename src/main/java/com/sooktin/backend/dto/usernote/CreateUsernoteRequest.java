package com.sooktin.backend.dto.usernote;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateUsernoteRequest {
    private String title;
    private String content;
    private Integer likes;
}
