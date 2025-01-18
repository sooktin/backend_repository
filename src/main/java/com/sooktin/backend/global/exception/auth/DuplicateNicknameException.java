package com.sooktin.backend.global.exception.auth;

public class DuplicateNicknameException extends DuplicateResourceException {
    public DuplicateNicknameException() {
        super("이미 사용 중인 닉네임입니다.");
    }
}
