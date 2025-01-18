package com.sooktin.backend.dto.email;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class EmailCheckRequest {
    @NotBlank
    @Email
    private String email;
}