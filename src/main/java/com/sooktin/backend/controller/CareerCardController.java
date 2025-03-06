package com.sooktin.backend.controller;

import com.sooktin.backend.domain.CareerCard;
import com.sooktin.backend.domain.User;
import com.sooktin.backend.dto.ResponseDto;
import com.sooktin.backend.dto.careercard.CreateCareerCardRequest;
import com.sooktin.backend.dto.careercard.CreateCareerCardResponse;
import com.sooktin.backend.dto.careercard.SearchCareerCardResponse;
import com.sooktin.backend.global.util.ResponseUtil;
import com.sooktin.backend.service.CareerCardService;
import com.sooktin.backend.service.CustomUserDetails;
import com.sooktin.backend.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/career-cards")
public class CareerCardController {

    private final CareerCardService careerCardService;
    private final UserService userService;

    private void validateOwnership(CareerCard careerCard, Long userId) {
        if (!careerCard.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
    }

    // R - 모든 CareerCard 목록 조회
    @GetMapping("/all")
    public ResponseEntity<ResponseDto<List<CreateCareerCardResponse>>> getAllCareerCards() {
        try {
            List<CreateCareerCardResponse> careerCards = careerCardService.findAll().stream()
                    .map(CreateCareerCardResponse::new)
                    .collect(Collectors.toList());
            return ResponseUtil.buildResponse(200, "커리어카드를 성공적으로 조회했습니다.", careerCards);
        } catch (Exception e) {
            return ResponseUtil.buildResponse(500, "커리어카드 목록을 조회하는 중 오류가 발생했습니다.", null);
        }
    }

    // R - 로그인된 사용자의 CareerCard 조회
    @GetMapping
    public ResponseEntity<ResponseDto<CreateCareerCardResponse>> getCareerCardByUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseUtil.buildResponse(401, "인증 정보가 유효하지 않습니다. 다시 로그인해주세요.", null);
            }

            CareerCard careerCard = careerCardService.findByUserId(userDetails.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("현재 로그인된 사용자의 커리어카드를 찾을 수 없습니다."));

            validateOwnership(careerCard, userDetails.getUserId());

            return ResponseUtil.buildResponse(200, "커리어카드를 성공적으로 조회했습니다.", new CreateCareerCardResponse(careerCard));
        } catch (IllegalArgumentException e) {
            return ResponseUtil.buildResponse(404, e.getMessage(), null);
        } catch (Exception e) {
            return ResponseUtil.buildResponse(500, "커리어카드를 조회하는 중 오류가 발생했습니다.", null);
        }
    }

    // R - 특정 CareerCard 조회
    @GetMapping("/{cardId}")
    public ResponseEntity<ResponseDto<CreateCareerCardResponse>> getCareerCardById(@PathVariable Long cardId) {
        try {
            CareerCard careerCard = careerCardService.findByCardId(cardId)
                    .orElseThrow(() -> new IllegalArgumentException("커리어카드를 찾을 수 없습니다."));

            return ResponseUtil.buildResponse(200, "커리어카드를 성공적으로 조회했습니다.", new CreateCareerCardResponse(careerCard));
        } catch (IllegalArgumentException e) {
            return ResponseUtil.buildResponse(404, e.getMessage(), null);
        } catch (Exception e) {
            return ResponseUtil.buildResponse(500, "커리어카드를 조회하는 중 오류가 발생했습니다.", null);
        }
    }

    // R - 로그인된 사용자의 CareerCard 이미지 목록 조회
    @GetMapping("/images")
    public ResponseEntity<ResponseDto<List<String>>> getMyCareerCardImages(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseUtil.buildResponse(401, "인증 정보가 유효하지 않습니다. 다시 로그인해주세요.", null);
            }

            CareerCard careerCard = careerCardService.findByUserId(userDetails.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("현재 로그인된 사용자의 커리어카드를 찾을 수 없습니다."));

            List<String> imageUrls = careerCard.getImageUrls();

            if (imageUrls.isEmpty()) {
                return ResponseUtil.buildResponse(204, "사용자의 커리어카드에 등록된 이미지가 없습니다.", null);
            }

            return ResponseUtil.buildResponse(200, "로그인된 사용자의 커리어카드 이미지 목록 조회 성공", imageUrls);

        } catch (IllegalArgumentException e) {
            return ResponseUtil.buildResponse(404, e.getMessage(), null);
        } catch (Exception e) {
            return ResponseUtil.buildResponse(500, "로그인된 사용자의 커리어카드 이미지를 조회하는 중 오류가 발생했습니다.", null);
        }
    }

    // R - 특정 사용자의 CareerCard 이미지 목록 조회
    @GetMapping("/{userId}/images")
    public ResponseEntity<ResponseDto<List<String>>> getCareerCardImagesByUserId(@PathVariable Long userId) {
        try {
            CareerCard careerCard = careerCardService.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 사용자의 커리어카드를 찾을 수 없습니다."));

            List<String> imageUrls = careerCard.getImageUrls();

            if (imageUrls.isEmpty()) {
                return ResponseUtil.buildResponse(204, "해당 사용자의 커리어카드에 등록된 이미지가 없습니다.", null);
            }

            return ResponseUtil.buildResponse(200, "해당 사용자의 커리어카드 이미지 목록 조회 성공", imageUrls);

        } catch (IllegalArgumentException e) {
            return ResponseUtil.buildResponse(404, e.getMessage(), null);
        } catch (Exception e) {
            return ResponseUtil.buildResponse(500, "해당 사용자의 커리어카드 이미지를 조회하는 중 오류가 발생했습니다.", null);
        }
    }

    // C - CareerCard 생성 (이미지 포함)
    @PostMapping
    public ResponseEntity<ResponseDto<CreateCareerCardResponse>> createCareerCard(
            @RequestPart(value = "files", required = false) List<MultipartFile> files,  // 이미지 파일
            @RequestPart(value = "request") @Valid CreateCareerCardRequest request,  // JSON 데이터 (DTO)
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseUtil.buildResponse(401, "인증 정보가 유효하지 않습니다. 다시 로그인해주세요.", null);
            }

            User user = userService.findUserByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            CareerCard createdCard = careerCardService.createCareerCard(request, user, files);
            return ResponseUtil.buildResponse(201, "커리어카드를 성공적으로 생성했습니다.", new CreateCareerCardResponse(createdCard));
        } catch (IllegalArgumentException e) {
            return ResponseUtil.buildResponse(400, e.getMessage(), null);
        } catch (Exception e) {
            return ResponseUtil.buildResponse(500, "커리어카드를 생성하는 중 오류가 발생했습니다: " + e.getMessage(), null);
        }
    }

    // U - CareerCard 수정 (새로운 이미지 포함)
    @PatchMapping
    public ResponseEntity<ResponseDto<CreateCareerCardResponse>> updateCareerCard(
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestPart(value = "request") @Valid CreateCareerCardRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseUtil.buildResponse(401, "인증 정보가 유효하지 않습니다. 다시 로그인해주세요.", null);
            }

            User user = userService.findUserByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            CareerCard updatedCard = careerCardService.updateCareerCard(request, user, files);
            return ResponseUtil.buildResponse(200, "커리어카드를 성공적으로 수정했습니다.", new CreateCareerCardResponse(updatedCard));
        } catch (IllegalArgumentException e) {
            return ResponseUtil.buildResponse(400, e.getMessage(), null);
        } catch (Exception e) {
            return ResponseUtil.buildResponse(500, "커리어카드를 수정하는 중 오류가 발생했습니다.", null);
        }
    }

    // D - CareerCard 삭제 (S3 이미지도 삭제)
    @DeleteMapping
    public ResponseEntity<ResponseDto<Void>> deleteCareerCard(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseUtil.buildResponse(401, "인증 정보가 유효하지 않습니다. 다시 로그인해주세요.", null);
            }

            CareerCard careerCard = careerCardService.findByUserId(userDetails.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("현재 로그인된 사용자의 커리어카드를 찾을 수 없습니다."));

            validateOwnership(careerCard, userDetails.getUserId());

            careerCardService.deleteById(careerCard.getId());
            return ResponseUtil.buildResponse(204, "커리어카드를 성공적으로 삭제했습니다.", null);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.buildResponse(400, e.getMessage(), null);
        } catch (Exception e) {
            return ResponseUtil.buildResponse(500, "커리어카드를 삭제하는 중 오류가 발생했습니다.", null);
        }
    }

    //TODO 우선 일케하고 그라파나,JMeter로 WebFlux와의 작용 보자
    @GetMapping("/search")
    public ResponseEntity<ResponseDto<SearchCareerCardResponse>> searchCareerCards(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @NotBlank(message = "검색어는 필수 입력값입니다") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)
    {

        SearchCareerCardResponse response = careerCardService.searchWithDtos(keyword, page, size);
        return ResponseEntity.ok(new ResponseDto<>(
                200,
                "검색 성공",
                response
        ));

    }


}