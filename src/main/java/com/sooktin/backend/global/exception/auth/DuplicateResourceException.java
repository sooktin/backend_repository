package com.sooktin.backend.global.exception.auth;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
      super(message);
    }
}
