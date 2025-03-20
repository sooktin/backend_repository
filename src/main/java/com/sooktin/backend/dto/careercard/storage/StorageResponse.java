package com.sooktin.backend.dto.careercard.storage;

import com.sooktin.backend.dto.careercard.CareerCardDTO;
import lombok.Data;

import java.util.List;

@Data
public class StorageResponse {
    private List<CardStorageItem> cardStorage;

    @Data
    public static class CardStorageItem {
        private CareerCardDTO careerCard;
    }

}
