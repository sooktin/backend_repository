package com.sooktin.backend.global.exception.auth;

public class DuplicateEmailException extends DuplicateResourceException {
    public DuplicateEmailException() {
        super("이미 존재하는 이메일입니다.");
    }
}
