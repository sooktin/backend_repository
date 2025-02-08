package com.sooktin.backend.dto.careercard.storage;

import com.sooktin.backend.domain.CareerCard;
import com.sooktin.backend.dto.ResponseDto;

public class GetStorageResponse extends ResponseDto<CareerCard> {
    public GetStorageResponse(int statusCode, String message, CareerCard data) {
        super(statusCode, message, data);
    }

}
