package com.sooktin.backend.service;

import com.sooktin.backend.domain.CareerCard;
import com.sooktin.backend.domain.CareerCardStorage;
import com.sooktin.backend.domain.StorageCardMapping;
import com.sooktin.backend.dto.careercard.storage.GetStorageResponse;
import com.sooktin.backend.repository.CareerCardRepository;
import com.sooktin.backend.repository.StorageCardMappingRepository;
import com.sooktin.backend.repository.StorageRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final StorageRepository storageRepository;
    private final CareerCardRepository careerCardRepository;
    private final StorageCardMappingRepository storageCardMappingRepository;

    // jpa 쓰지 않고 스프링 시쿠리티로 찾아올수있음
    @Transactional(readOnly = true)
    public GetStorageResponse getCardsFromStorage(Long userId) {
        CareerCardStorage storage = storageRepository.findByUserIdWithQuery(userId)
                .orElseThrow(()->new IllegalArgumentException("보관함을 찾을 수 없습니다."));

        return new GetStorageResponse(200,"커리어카드 보관함을 갖고 옵니다.", storage);

    }

    @Transactional
    public String saveCardsToStorage(Long careerCardId) {
        Long userId = getCurrentUserId();
        CareerCardStorage storage = storageRepository.findByUserIdWithQuery(userId)
                .orElseThrow(()->new IllegalArgumentException("보관함을 찾을 수 없습니다."));
        CareerCard careerCard = careerCardRepository.findById(careerCardId)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 커리어카드입니다."));
        StorageCardMapping mapping = new StorageCardMapping();
        mapping.setCareerCardStorage(storage);
        mapping.setCareerCard(careerCard);

        storageCardMappingRepository.save(mapping);

        return "커리어카드가 보관함에 성공적으로 추가되었습니다.";
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails)principal).getUserId();
        }
        throw new RuntimeException("로그인된 사용자가 없습니다.");
    }
}
