package com.sooktin.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseDto<T> {
    private int statusCode;        // HTTP 상태 코드
    private String message;      // 메시지
    private T data;          // 실제 데이터
}