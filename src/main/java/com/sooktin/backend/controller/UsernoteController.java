package com.sooktin.backend.controller;

import com.sooktin.backend.domain.User;
import com.sooktin.backend.domain.Usernote;
import com.sooktin.backend.dto.ResponseDto;
import com.sooktin.backend.dto.usernote.CreateUsernoteRequest;
import com.sooktin.backend.dto.usernote.CreateUsernoteResponse;
import com.sooktin.backend.global.util.ResponseUtil;
import com.sooktin.backend.service.UserService;
import com.sooktin.backend.service.CustomUserDetails;
import com.sooktin.backend.service.UsernoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/usernotes")
public class UsernoteController {

    /**
     * 클라이언트(우리는 리액트)에게 전달합니다.
     *  controller에서 @GetMapping() { } 등등이 쓰임
     **/

    private final UsernoteService usernoteService;
    private final UserService userService;


    // R - 전체 게시글 조회
    @GetMapping
    public ResponseEntity<ResponseDto<List<CreateUsernoteResponse>>> getAllUsernotes() {
        try {
            List<CreateUsernoteResponse> usernotes = usernoteService.findAll().stream()
                    .map(CreateUsernoteResponse::new)
                    .collect(Collectors.toList());
            return ResponseUtil.buildResponse(200, "게시글 목록을 불러왔습니다.", usernotes);
        } catch (IllegalStateException e) {
            return ResponseUtil.buildResponse(400, "잘못된 요청입니다." , null);
        } catch (Exception e) {
            return ResponseUtil.buildResponse(500, "게시글 목록을 불러오는 중 오류가 발생했습니다.", null);
        }
    }

    // 특정 포스트 조회
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<CreateUsernoteResponse>> getUsernoteById(@PathVariable Long id) {
        try {
            Optional<Usernote> usernote = usernoteService.findById(id);
            return usernote.map(note -> ResponseUtil.buildResponse(200, "게시글을 조회했습니다.", new CreateUsernoteResponse(note)))
                    .orElseGet(() -> ResponseUtil.buildResponse(404, "게시글을 찾을 수 없습니다.", null));
        } catch (Exception e) {
            return ResponseUtil.buildResponse(500, "게시글 조회 중 오류가 발생했습니다.", null);
        }
    }

    // 포스트 생성
    /* -> 추후에 파라미터로 @AuthenticationPrincipal UserDetails userDetails 넣고
          user 찾을 때 Optional<User> user = userService.findByEmail(userDetails.getUsername())도 고려해주세욤
    from 경민 to 수진
    */
    @PostMapping
    public ResponseEntity<ResponseDto<CreateUsernoteResponse>> createPost(
            @Valid @RequestBody CreateUsernoteRequest userNoteRequest, // 유효성 검사를 추가
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // 사용자 인증 확인
            if (userDetails == null) {
                return ResponseUtil.buildResponse(401, "인증 정보가 유효하지 않습니다. 다시 로그인해주세요.", null);
            }

            // 사용자 조회
            User user = userService.findUserByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            // 게시글 생성
            Usernote newUsernote = new Usernote();
            newUsernote.setContent(userNoteRequest.getContent());
            //newUsernote.setLikes(0); // 기본값 처리
            newUsernote.setUser(user);

            Usernote createdUsernote = usernoteService.createUsernote(newUsernote);
            return ResponseUtil.buildResponse(201, "게시글이 성공적으로 생성되었습니다.", new CreateUsernoteResponse(createdUsernote));
        } catch (IllegalArgumentException e) {
            // 잘못된 요청 처리
            return ResponseUtil.buildResponse(400, "게시글 생성 중 오류가 발생했습니다: " + e.getMessage(), null);
        } catch (Exception e) {
            // 기타 서버 오류 처리
            return ResponseUtil.buildResponse(500, "서버 내부 오류가 발생했습니다.", null);
        }
    }

    // 포스트 수정
    @PatchMapping("/{id}")
    public ResponseEntity<ResponseDto<CreateUsernoteResponse>> updateUsernote(
            @PathVariable Long id,
            @RequestBody CreateUsernoteRequest userNoteRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {

            if (userDetails == null) {
                return ResponseUtil.buildResponse(401, "인증 정보가 유효하지 않습니다. 다시 로그인해주세요.", null);
            }

            User user = userService.findUserByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            Usernote existingUsernote = usernoteService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

            if (!existingUsernote.getUser().getId().equals(user.getId())) {
                return ResponseUtil.buildResponse(403, "권한이 없습니다.", null);
            }

            existingUsernote.setContent(userNoteRequest.getContent());

            // 업데이트
            Usernote updatedUsernote = usernoteService.updateUsernote(id, existingUsernote);
            return ResponseUtil.buildResponse(200, "게시글이 성공적으로 수정되었습니다.", new CreateUsernoteResponse(updatedUsernote));
        } catch (IllegalArgumentException e) {
            return ResponseUtil.buildResponse(400, "게시글 수정 중 오류가 발생했습니다: " + e.getMessage(), null);
        } catch (Exception e) {
            return ResponseUtil.buildResponse(500, "서버 내부 오류가 발생했습니다.", null);
        }
    }


    // 포스트 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto<Void>> deleteUsernote(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseUtil.buildResponse(401, "인증 정보가 유효하지 않습니다. 다시 로그인해주세요.", null);
            }

            User user = userService.findUserByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            Usernote existingUsernote = usernoteService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

            if (!existingUsernote.getUser().getId().equals(user.getId())) {
                return ResponseUtil.buildResponse(403, "권한이 없습니다.", null);
            }

            // 게시글 삭제
            usernoteService.deleteById(id);
            return ResponseUtil.buildResponse(204, "게시글이 성공적으로 삭제되었습니다.", null);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.buildResponse(400, "게시글 삭제 중 오류가 발생했습니다: " + e.getMessage(), null);
        } catch (Exception e) {
            return ResponseUtil.buildResponse(500, "서버 내부 오류가 발생했습니다.", null);
        }
    }
}
