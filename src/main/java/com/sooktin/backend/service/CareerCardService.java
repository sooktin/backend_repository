package com.sooktin.backend.service;

import com.sooktin.backend.domain.CareerCard;
import com.sooktin.backend.domain.Experience;
import com.sooktin.backend.domain.User;
import com.sooktin.backend.dto.careercard.CareerCardMapper;
import com.sooktin.backend.dto.careercard.CreateCareerCardRequest;
import com.sooktin.backend.dto.careercard.SearchCareerCardResponse;
import com.sooktin.backend.repository.CareerCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CareerCardService {

    private static final int MAX_IMAGE_COUNT = 3;
    private final CareerCardRepository careerCardRepository;
    private final S3Service s3Service;
    private final CareerCardMapper careerCardMapper;

    // C - 커리어카드 생성 (S3 이미지 업로드 추가)
    public CareerCard createCareerCard(CreateCareerCardRequest request, User user, List<MultipartFile> files) {
        if (careerCardRepository.findByUserId(user.getId()).isPresent()) {
            throw new IllegalArgumentException("해당 유저는 이미 커리어카드를 가지고 있습니다.");
        }

        CareerCard careerCard = new CareerCard();
        careerCard.setUser(user);

        // 공통 필드 설정
        setCareerCardFields(careerCard, request);

        // 이미지 검증 및 업로드
        List<String> imageUrls = uploadImagesWithValidation(files);

        if (!imageUrls.isEmpty()) {
            careerCard.setImageUrls(imageUrls);
        }

        return careerCardRepository.save(careerCard);
    }

    // R - 모든 커리어카드 조회
    public List<CareerCard> findAll() {
        return careerCardRepository.findAll();
    }

    // R - 특정 ID로 커리어카드 조회
    @Cacheable(value = "careerCard", key = "#cardId")
    public Optional<CareerCard> findByCardId(Long cardId) {
        return careerCardRepository.findById(cardId);
    }

    // R - 특정 유저 ID로 커리어카드 조회
    @Cacheable(value = "careerCard", key = "#userId")
    public Optional<CareerCard> findByUserId(Long userId) {
        return careerCardRepository.findByUserId(userId);
    }


    // U - 커리어카드 수정 (S3 이미지 변경 가능)
    @CacheEvict(value = "careerCard", allEntries = true)
    @Transactional
    public CareerCard updateCareerCard(CreateCareerCardRequest request, User user, List<MultipartFile> files) {
        CareerCard careerCard = careerCardRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자의 커리어카드를 찾을 수 없습니다."));

        // 기존 이미지 삭제
        if (careerCard.getImageUrls() != null && !careerCard.getImageUrls().isEmpty()) {
            careerCard.getImageUrls().forEach(s3Service::deleteImage);
        }

        // 이미지 검증 및 업로드
        List<String> newImageUrls = uploadImagesWithValidation(files);

        careerCard.setImageUrls(newImageUrls);

        // 공통 필드 설정
        setCareerCardFields(careerCard, request);

        return careerCardRepository.save(careerCard);
    }


    // D - 커리어카드 삭제 (S3 이미지도 삭제)
    @Transactional
    @CacheEvict(value = "careerCard", key = "#cardId")
    public void deleteById(Long cardId) {
        CareerCard careerCard = careerCardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("해당 커리어카드를 찾을 수 없습니다. id: " + cardId));

        // S3 이미지 삭제
        if (careerCard.getImageUrls() != null && !careerCard.getImageUrls().isEmpty()) {
            careerCard.getImageUrls().forEach(s3Service::deleteImage);
        }

        // DB에서 삭제
        careerCardRepository.deleteById(cardId);
    }

    // 권한 검증 (현재 로그인한 유저가 본인 카드만 수정/삭제 가능)
    private void validateOwnership(CareerCard careerCard, Long userId) {
        if (!careerCard.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
    }

    // 이미지 검증 및 업로드 처리
    private List<String> uploadImagesWithValidation(List<MultipartFile> files) {
        List<String> imageUrls = new ArrayList<>();

        if (files != null) {
            if (files.size() > MAX_IMAGE_COUNT) {
                throw new IllegalArgumentException("이미지는 최대 " + MAX_IMAGE_COUNT + "개까지 업로드할 수 있습니다.");
            }

            for (MultipartFile file : files) {
                String uploadedUrl = s3Service.uploadImage(file);
                if (uploadedUrl != null) {  // ✅ null 값 추가 방지
                    imageUrls.add(uploadedUrl);
                }
            }
        }
        return imageUrls;
    }

    private void setCareerCardFields(CareerCard careerCard, CreateCareerCardRequest request) {
        careerCard.setJob(request.getJob());
        careerCard.setMajor(request.getMajor());
        careerCard.setStudent_status(request.getStudentStatus());
        careerCard.setGrade(Integer.valueOf(request.getGrade()));
        careerCard.setStudent_num(request.getStudentNum());
        careerCard.setDepartment(request.getDepartment());

        // ExperienceRequest → Experience 변환
        careerCard.setExperiences(request.getExperiences().stream()
                .map(exp -> new Experience(exp.getCompany(), exp.getPeriod()))
                .collect(Collectors.toList()));

        careerCard.setSkills(request.getSkills());
    }

    // 커리어카드 검색
    @Transactional(readOnly = true)
    @Cacheable(
            value = "careerCard",
            key = "'keyword_'+#keyword+'_page'+#page+'_size'+#size",
            unless = "#result.careerCards.isEmpty()"
    )
    public SearchCareerCardResponse searchWithDtos(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CareerCard> results = search(keyword, pageable);

        return new SearchCareerCardResponse(
                careerCardMapper.toDtoList(results.getContent()),
                (int) results.getTotalElements(),
                page,
                size
        );
    }


    // 커리어카드 검색
    public Page<CareerCard> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("검색어는 필수 입력값입니다.");
        }

        return careerCardRepository.searchCareerCardsWithOrCondition(keyword, pageable);
    }

}