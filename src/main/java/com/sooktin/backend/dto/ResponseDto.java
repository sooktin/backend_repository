package com.sooktin.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseDto<T> {
    @JsonProperty("statusCode")
    private int statusCode;        // HTTP 상태 코드

    @JsonProperty("message")
    private String message;      // 메시지

    @JsonProperty("data")
    private T data;          // 실제 데이터
}