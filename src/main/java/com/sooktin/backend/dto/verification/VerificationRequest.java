package com.sooktin.backend.dto.verification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class VerificationRequest {
    private String email;
    private String code;
}
