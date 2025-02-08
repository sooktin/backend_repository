package com.sooktin.backend.controller;

import com.sooktin.backend.auth.JwtUtil;
import com.sooktin.backend.domain.CareerCard;
import com.sooktin.backend.domain.User;
import com.sooktin.backend.dto.ResponseDto;
import com.sooktin.backend.dto.careercard.storage.GetStorageResponse;
import com.sooktin.backend.dto.user.NicknameRequest;
import com.sooktin.backend.dto.user.NicknameResponse;
import com.sooktin.backend.dto.user.UserGetResponse;
import com.sooktin.backend.dto.usernote.FindMyUsernoteWithJWTResponse;
import com.sooktin.backend.repository.UserRepository;
import com.sooktin.backend.service.CustomUserDetails;
import com.sooktin.backend.service.StorageService;
import com.sooktin.backend.service.UserService;
import com.sooktin.backend.service.UsernoteService;
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

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UsernoteService usernoteService;
    private final StorageService storageService;

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

}
