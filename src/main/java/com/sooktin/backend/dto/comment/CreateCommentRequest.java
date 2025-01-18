package com.sooktin.backend.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class CreateCommentRequest {
    private String content;
    private Integer likes; // 좋아요 수 (Optional)

    // Getter, Setter
}