package com.sooktin.backend.controller;

import com.sooktin.backend.auth.JwtUtil;
import com.sooktin.backend.domain.CareerCard;
import com.sooktin.backend.domain.User;
import com.sooktin.backend.dto.ResponseDto;
import com.sooktin.backend.dto.careercard.storage.CcgotoStorageRequest;
import com.sooktin.backend.dto.careercard.storage.CcgotoStorageResponse;
import com.sooktin.backend.dto.careercard.storage.GetStorageResponse;
import com.sooktin.backend.dto.user.NicknameRequest;
import com.sooktin.backend.dto.user.NicknameResponse;
import com.sooktin.backend.dto.user.UserGetResponse;
import com.sooktin.backend.dto.usernote.FindMyUsernoteWithJWTResponse;
import com.sooktin.backend.global.util.ResponseUtil;
import com.sooktin.backend.repository.StorageCardMappingRepository;
import com.sooktin.backend.repository.UserRepository;
import com.sooktin.backend.service.*;
import io.micrometer.core.annotation.Timed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.sooktin.backend.domain.QCareerCard.careerCard;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UsernoteService usernoteService;
    private final StorageService storageService;
    private final CareerCardService careerCardService;
    private final StorageCardMappingRepository storageCardMappingRepository;

    //delete되는지 가라 기능 작업 수행임
    @GetMapping("/search")
    public ResponseEntity<Optional<User>> searchUsers(@RequestParam String nickname) {
        return ResponseEntity.ok((userService.search(nickname)));
    }

    @GetMapping
    public ResponseEntity<?> getUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Optional<User> user = userService.findUserById(userDetails.getUserId());
        return user.map(u -> ResponseEntity.ok(UserGetResponse.from(u)))
                .orElseGet(() -> ResponseEntity.notFound().build()); //elseget은 매개값이 필요할때만
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUser(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
            }

            String userEmail = jwtUtil.getEmailFromToken(token);

            userService.delete(userEmail);
            return ResponseEntity.ok().body("회원탈퇴성공");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/usernotes")
    public ResponseEntity<List<FindMyUsernoteWithJWTResponse>> getUserNotes(@AuthenticationPrincipal UserDetails userDetails) {
        List<FindMyUsernoteWithJWTResponse> responseList = usernoteService.findByUserEmail(userDetails.getUsername());
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/nickname")
    public ResponseEntity<ResponseDto<String>> getNickname(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ResponseDto<>(401, "인증 정보가 유효하지 않습니다. 다시 로그인해주세요.", null));
            }

            String nickname = userDetails.getNickname();

            // 정상 응답 반환
            return ResponseEntity.ok(new ResponseDto<>(200, "닉네임 조회 성공", nickname));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto<>(500, "서버 내부 오류가 발생했습니다. 다시 시도해주세요.", null));
        }
    }

    // R - 로그인된 사용자의 메인 이미지(커리어카드 첫 번째 이미지) 반환
    @GetMapping("/main-image")
    public ResponseEntity<ResponseDto<String>> getMyMainImage(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseUtil.buildResponse(401, "인증 정보가 유효하지 않습니다. 다시 로그인해주세요.", null);
            }

            CareerCard careerCard = careerCardService.findByUserId(userDetails.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("현재 로그인된 사용자의 커리어카드를 찾을 수 없습니다."));

            List<String> imageUrls = careerCard.getImageUrls();

            if (imageUrls.isEmpty()) {
                return ResponseUtil.buildResponse(404, "로그인된 사용자의 프로필 이미지가 없습니다.", null);
            }

            String profileImage = imageUrls.get(0); // 첫 번째 이미지 선택
            return ResponseUtil.buildResponse(200, "로그인된 사용자의 프로필 이미지 조회 성공", profileImage);

        } catch (IllegalArgumentException e) {
            return ResponseUtil.buildResponse(404, e.getMessage(), null);
        } catch (Exception e) {
            return ResponseUtil.buildResponse(500, "로그인된 사용자의 프로필 이미지를 조회하는 중 오류가 발생했습니다.", null);
        }
    }

    // R - 특정 사용자의 메인 이미지(커리어카드 첫 번째 이미지) 반환
    @GetMapping("/{userId}/main-image")
    public ResponseEntity<ResponseDto<String>> getUserMainImage(@PathVariable Long userId) {
        try {
            CareerCard careerCard = careerCardService.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 사용자의 커리어카드를 찾을 수 없습니다."));

            List<String> imageUrls = careerCard.getImageUrls();

            if (imageUrls.isEmpty()) {
                return ResponseUtil.buildResponse(404, "해당 사용자의 프로필 이미지가 없습니다.", null);
            }

            String profileImage = imageUrls.get(0); // 첫 번째 이미지 선택
            return ResponseUtil.buildResponse(200, "해당 사용자의 프로필 이미지 조회 성공", profileImage);

        } catch (IllegalArgumentException e) {
            return ResponseUtil.buildResponse(404, e.getMessage(), null);
        } catch (Exception e) {
            return ResponseUtil.buildResponse(500, "해당 사용자의 프로필 이미지를 조회하는 중 오류가 발생했습니다.", null);
        }
    }

    @PatchMapping("/nickname")
    public ResponseEntity<NicknameResponse> chacngeNickname(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody @Valid NicknameRequest nicknameRequest) {
        NicknameResponse response = userService.changeNickname(userDetails.getNickname(), nicknameRequest.getNickname());
        return ResponseEntity.ok(response);
    }


    //CCS를 반환하면 id,userID등불필요한 데이터도 반환하기에 리스트형태의 CC 반환
    @Timed(
            value = "get.user.cardstorage",
            description = "Time taken to get user's card storage",
            percentiles = {0.5, 0.95, 0.99},
            histogram = true
    )
    @GetMapping("/card-storage")
    public ResponseEntity<GetStorageResponse> getUserCardStorage(@AuthenticationPrincipal CustomUserDetails userDetails) {
        GetStorageResponse response = storageService.getCardsFromStorage(userDetails.getUserId());

        return ResponseEntity.ok(response);
    }

    //TODO String 반환말고 다른 좋을게잇을듯함 서비스 코드도 리팩터 필요
    @PostMapping("/card-storage/career-cards")
    public ResponseEntity<String> saveCareerCard(@RequestParam Long careerCardId) {
        String message = storageService.saveCardsToStorage(careerCardId);
        //boolean으로중복값
;        return ResponseEntity.ok(message);
    }

    //삭제 기능
    //TODO @DeleteMapping("/card-storage/career-cards")

}
