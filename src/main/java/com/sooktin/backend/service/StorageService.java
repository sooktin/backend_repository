package com.sooktin.backend.service;

import com.sooktin.backend.domain.CareerCard;
import com.sooktin.backend.domain.CareerCardStorage;
import com.sooktin.backend.repository.StorageRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final StorageRepository storageRepository;

    @Transactional(readOnly = true)
    public List<CareerCard> getCardsFromStorage(Long userId) {
        CareerCardStorage storage = storageRepository.findByUserIdWithQuery(userId)
                .orElseThrow(()->new IllegalArgumentException("보관함을 찾을 수 없습니다."));

        return storage.getCareerCards();
    }
}
