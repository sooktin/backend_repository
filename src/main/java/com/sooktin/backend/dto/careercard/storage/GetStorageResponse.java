package com.sooktin.backend.dto.careercard.storage;

import com.sooktin.backend.domain.CareerCard;
import com.sooktin.backend.domain.CareerCardStorage;
import com.sooktin.backend.dto.ResponseDto;

public class GetStorageResponse extends ResponseDto<CareerCardStorage> {
    public GetStorageResponse(int statusCode, String message, CareerCardStorage data) {
        super(statusCode, message, data);
    }

}
