package com.sooktin.backend.dto.careercard.storage;

import com.sooktin.backend.domain.CareerCard;
import com.sooktin.backend.dto.ResponseDto;

public class CcgotoStorageResponse extends ResponseDto<CareerCard> {
    public CcgotoStorageResponse(int statusCode, String message, CareerCard data) {
        super(statusCode, message, data);
    }
}

