package com.sooktin.backend.dto.email;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
public class EmailCheckRequest {
    @NotBlank
    @Email
    private String email;

    public EmailCheckRequest(String email) {
        this.email = email;
    }

    public EmailCheckRequest() {}
}