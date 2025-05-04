package com.sooktin.backend.service;

import com.sooktin.backend.domain.CareerCard;
import com.sooktin.backend.domain.CareerCardStorage;
import com.sooktin.backend.domain.StorageCardMapping;
import com.sooktin.backend.domain.User;
import com.sooktin.backend.dto.ResponseDto;
import com.sooktin.backend.dto.careercard.CareerCardDTO;
import com.sooktin.backend.dto.careercard.storage.StorageResponse;
import com.sooktin.backend.repository.CareerCardRepository;
import com.sooktin.backend.repository.StorageCardMappingRepository;
import com.sooktin.backend.repository.StorageRepository;
import com.sooktin.backend.repository.UserRepository;
import com.sooktin.backend.service.alarm.AlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final StorageRepository storageRepository;
    private final CareerCardRepository careerCardRepository;
    private final StorageCardMappingRepository storageCardMappingRepository;
    private final UserRepository userRepository;
    private final AlarmService alarmService;

    // jpa 쓰지 않고 스프링 시쿠리티로 찾아올수있음
    @Transactional(readOnly = true)
    public ResponseDto<StorageResponse> getCardsFromStorage(Long userId) {
        List<Object[]> results = storageRepository.findCareerCardsByUserId(userId);
        if (results.isEmpty()) {
            return new ResponseDto<>(404, "보관함에 카드가 없습니다.", null);
        }
        StorageResponse storageResponse = new StorageResponse();
        List<StorageResponse.CardStorageItem> cardStorageItems = results.stream().map(result -> {
            CareerCard careerCard = (CareerCard) result[0];
            String nickname = (String) result[1];

            CareerCardDTO dto = CareerCardDTO.builder()
                    .cardId(careerCard.getId())
                    .nickname(nickname)
                    .major(careerCard.getMajor())
                    .student_num(careerCard.getStudent_num())
                    .student_status(careerCard.getStudent_status())
                    .grade(careerCard.getGrade())
                    .userId(careerCard.getUser().getId())
                    .department(careerCard.getDepartment())
                    .skills(careerCard.getSkills())
                    .job(careerCard.getJob())
                    .experiences(careerCard.getExperiences().stream()
                            .map(exp -> CareerCardDTO.ExperienceDTO.builder()
                                    .company(exp.getCompany())
                                    .period(exp.getPeriod())
                                    .build())
                            .collect(Collectors.toList()))
                    .imageUrls(careerCard.getImageUrls())
                    .build();
            StorageResponse.CardStorageItem item = new StorageResponse.CardStorageItem();
            item.setCareerCard(dto);
            return item;
        }).collect(Collectors.toList());

        storageResponse.setCardStorage(cardStorageItems);
        return new ResponseDto<>(200, "보관된 카드 ID 조회 성공", storageResponse);
    }

    @Transactional
    public ResponseDto<Long> saveCardsToStorage(Long careerCardId) {
        Long userId = getCurrentUserId();
        List<Object[]> storageList = storageRepository.findByUserIdWithQuery(userId);
        if (storageList.isEmpty()) {
            throw new IllegalArgumentException("보관함이 없습니다.");
        }
        //TODO 왜첫번째여야하지? 걍 개인당 하나 소유이긴함
        CareerCardStorage storage = (CareerCardStorage) storageList.get(0)[1]; // 첫 번째 보관함 사용
        CareerCard careerCard = careerCardRepository.findById(careerCardId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 커리어카드입니다."));

        User cardOwner = careerCard.getUser();
        if (cardOwner == null) {
            throw new IllegalArgumentException("해당 커리어카드의 유저를 찾을 수 없습니다.");
        }
        Long cardOwnerId = cardOwner.getId();

        // 이미 매칭되어 있는지 확인
        boolean isMatched = storageCardMappingRepository.existsByCareerCard_UserIdAndCareerCardStorage_UserId(
                userId, cardOwnerId
        );

        // 매치 알림 전송
        if (isMatched && !userId.equals(cardOwnerId)) {
            User me = userRepository.findById(userId).orElseThrow();
            alarmService.sendMatchAlarm(me, cardOwner);
        }

        StorageCardMapping mapping = new StorageCardMapping();
        mapping.setCareerCardStorage(storage);
        mapping.setCareerCard(careerCard);
        StorageCardMapping savedMapping = storageCardMappingRepository.save(mapping);
        return new ResponseDto<>(200,"커리어카드가 보관함에 성공적으로 추가되었습니다.", savedMapping.getId());
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails)principal).getUserId();
        }
        throw new RuntimeException("로그인된 사용자가 없습니다.");
    }

    public ResponseDto<Long> deleteCardsFromStorage(Long careerCardId) {
        Long userId = getCurrentUserId();
        CareerCardStorage storage = storageRepository.findByUserId(userId)
                .orElseThrow(()-> new IllegalArgumentException("보관함이 없습니다.")); //보관함 찾기!
        StorageCardMapping mapping = storageCardMappingRepository.findByCareerCardStorageAndCareerCardId(storage, careerCardId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카드입니다."));
        storageCardMappingRepository.delete(mapping);
        return new ResponseDto<>(200, "커리어카드가 보관함에서 성공적으로 삭제되었습니다.", mapping.getId());
    }
}
