package com.sooktin.backend.global;

public class InvalidTwoFaCodeException extends RuntimeException {
    public InvalidTwoFaCodeException(String s) {
        super(s);
    }
}
