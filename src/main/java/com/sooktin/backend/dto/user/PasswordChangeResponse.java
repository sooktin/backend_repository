package com.sooktin.backend.dto.user;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PasswordChangeResponse {
    private int status;
    private String message;
}
