package com.sooktin.backend.global.exception;

public class InvalidTwoFaCodeException extends RuntimeException {
    public InvalidTwoFaCodeException(String s) {
        super(s);
    }
}