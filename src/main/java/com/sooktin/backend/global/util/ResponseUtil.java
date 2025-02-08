package com.sooktin.backend.global.util;

import com.sooktin.backend.dto.ResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ResponseUtil {
    public static <T> ResponseEntity<ResponseDto<T>> buildResponse(int statusCode, String message, T data) {
        return ResponseEntity.status(statusCode)
                .body(new ResponseDto<>(statusCode, message, data));
    }
}