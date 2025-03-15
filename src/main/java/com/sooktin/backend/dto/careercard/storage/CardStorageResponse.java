package com.sooktin.backend.dto.careercard.storage;

import com.sooktin.backend.domain.CareerCardStorage;
import com.sooktin.backend.dto.ResponseDto;
import lombok.Data;
import lombok.Getter;

@Getter
public class CardStorageResponse extends ResponseDto<CardStorageResponse.CardToHome> {
    public CardStorageResponse(int statusCode, String message, CardToHome data) {
        super(statusCode, message, data);
    }



    @Data
    public static class CardToHome {
        private Long cardId;
        private CareerCardStorage careerCardStorage;
    }
}

