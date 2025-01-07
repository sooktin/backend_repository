package com.sooktin.backend.controller;

import com.sooktin.backend.domain.CareerCard;
import com.sooktin.backend.domain.User;
import com.sooktin.backend.dto.ResponseDto;
import com.sooktin.backend.dto.careercard.CreateCareerCardRequest;
import com.sooktin.backend.dto.careercard.CreateCareerCardResponse;
import com.sooktin.backend.repository.UserRepository;
import com.sooktin.backend.service.CareerCardService;
import com.sooktin.backend.service.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/career-card")
public class CareerCardController {

    private final CareerCardService careerCardService;
    private final UserRepository userRepository;

    @Autowired
    public CareerCardController(CareerCardService careerCardService, UserRepository userRepository) {
        this.careerCardService = careerCardService;
        this.userRepository = userRepository;
    }

    private <T> ResponseEntity<ResponseDto<T>> buildResponse(int statusCode, String message, T data) {
        return ResponseEntity.status(statusCode)
                .body(new ResponseDto<>(statusCode, message, data));
    }

    private void validateOwnership(CareerCard careerCard, Long userId) {
        if (!careerCard.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
    }

    // R - 모든 CareerCard 목록 조회(개발자용)
    @GetMapping("/all")
    public ResponseEntity<ResponseDto<List<CreateCareerCardResponse>>> getAllCareerCards() {
        try {
            List<CreateCareerCardResponse> careerCards = careerCardService.findAll().stream()
                    .map(CreateCareerCardResponse::new)
                    .collect(Collectors.toList());
            return buildResponse(200, "커리어카드를 성공적으로 조회했습니다.", careerCards);
        } catch (Exception e) {
            return buildResponse(500, "커리어카드 목록을 조회하는 중 오류가 발생했습니다.", null);
        }
    }


    // R - 로그인된 사용자의 CareerCard 조회
    @GetMapping
    public ResponseEntity<ResponseDto<CreateCareerCardResponse>> getCareerCardByUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            if (userDetails == null) {
                return buildResponse(401, "인증 정보가 유효하지 않습니다. 다시 로그인해주세요.", null);
            }

            CareerCard careerCard = careerCardService.findByUserId(userDetails.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("현재 로그인된 사용자의 커리어카드를 찾을 수 없습니다."));

            validateOwnership(careerCard, userDetails.getUserId());

            return buildResponse(200, "커리어카드를 성공적으로 조회했습니다.", new CreateCareerCardResponse(careerCard));
        } catch (IllegalArgumentException e) {
            return buildResponse(404, e.getMessage(), null);
        } catch (Exception e) {
            return buildResponse(500, "커리어카드를 조회하는 중 오류가 발생했습니다.", null);
        }
    }


    // R - 특정 CareerCard 조회 (다른 유저의 카드)
    @GetMapping("/{cardId}")
    public ResponseEntity<ResponseDto<CreateCareerCardResponse>> getCareerCardById(@PathVariable Long cardId) {
        try {
            CareerCard careerCard = careerCardService.findByCardId(cardId)
                    .orElseThrow(() -> new IllegalArgumentException("커리어카드를 찾을 수 없습니다."));

            return buildResponse(200, "커리어카드를 성공적으로 조회했습니다.", new CreateCareerCardResponse(careerCard));
        } catch (IllegalArgumentException e) {
            return buildResponse(404, e.getMessage(), null);
        } catch (Exception e) {
            return buildResponse(500, "커리어카드를 조회하는 중 오류가 발생했습니다.", null);
        }
    }

    // C - CareerCard 생성
    @PostMapping
    public ResponseEntity<ResponseDto<CreateCareerCardResponse>> createCareerCard(
            @Valid @RequestBody CreateCareerCardRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            if (userDetails == null) {
                return buildResponse(401, "인증 정보가 유효하지 않습니다. 다시 로그인해주세요.", null);
            }

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            CareerCard careerCard = mapToCareerCard(request, user);
            CareerCard createdCard = careerCardService.createCareerCard(careerCard);

            return buildResponse(201, "커리어카드를 성공적으로 생성했습니다.", new CreateCareerCardResponse(createdCard));
        } catch (IllegalArgumentException e) {
            return buildResponse(400, e.getMessage(), null);
        } catch (Exception e) {
            return buildResponse(500, "커리어카드를 생성하는 중 오류가 발생했습니다.", null);
        }
    }

    // U - CareerCard 수정
    @PatchMapping
    public ResponseEntity<ResponseDto<CreateCareerCardResponse>> updateCareerCard(
            @Valid @RequestBody CreateCareerCardRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            if (userDetails == null) {
                return buildResponse(401, "인증 정보가 유효하지 않습니다. 다시 로그인해주세요.", null);
            }

            CareerCard careerCard = careerCardService.findByUserId(userDetails.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("현재 로그인된 사용자의 커리어카드를 찾을 수 없습니다."));

            validateOwnership(careerCard, userDetails.getUserId());

            CareerCard updatedCard = careerCardService.updateCareerCard(careerCard.getId(), mapToCareerCard(request, careerCard.getUser()));

            return buildResponse(200, "커리어카드를 성공적으로 수정했습니다.", new CreateCareerCardResponse(updatedCard));
        } catch (IllegalArgumentException e) {
            return buildResponse(400, e.getMessage(), null);
        } catch (Exception e) {
            return buildResponse(500, "커리어카드를 수정하는 중 오류가 발생했습니다.", null);
        }
    }

    // D - CareerCard 삭제
    @DeleteMapping
    public ResponseEntity<ResponseDto<Void>> deleteCareerCard(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            if (userDetails == null) {
                return buildResponse(401, "인증 정보가 유효하지 않습니다. 다시 로그인해주세요.", null);
            }

            CareerCard careerCard = careerCardService.findByUserId(userDetails.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("현재 로그인된 사용자의 커리어카드를 찾을 수 없습니다."));

            validateOwnership(careerCard, userDetails.getUserId());

            careerCardService.deleteById(careerCard.getId());
            return buildResponse(204, "커리어카드를 성공적으로 삭제했습니다.", null);
        } catch (IllegalArgumentException e) {
            return buildResponse(400, e.getMessage(), null);
        } catch (Exception e) {
            return buildResponse(500, "커리어카드를 삭제하는 중 오류가 발생했습니다.", null);
        }
    }

    // DTO -> Entity 변환
    private CareerCard mapToCareerCard(CreateCareerCardRequest request, User user) {
        CareerCard careerCard = new CareerCard();
        careerCard.setUser(user);
        careerCard.setMajor(request.getMajor());
        careerCard.setStudent_status(request.getStudentStatus());
        careerCard.setGrade(request.getGrade());
        careerCard.setStudent_num(request.getStudentNum());
        careerCard.setDepartments(request.getDepartments());
        careerCard.setExperiences(request.getExperiences());
        careerCard.setSkills(request.getSkills());
        careerCard.setImageUrls(request.getImageUrls());
        careerCard.setNickname(request.getNickname());
        return careerCard;
    }
}