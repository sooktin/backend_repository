package com.sooktin.backend.dto.usernote;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsernoteDTO {
    private Long id;
    private String content;
    private Long userId;

}
