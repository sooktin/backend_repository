package com.sooktin.backend.controller;

import com.sooktin.backend.domain.User;
import com.sooktin.backend.domain.Usernote;
import com.sooktin.backend.dto.usernote.CreateUsernoteRequest;
import com.sooktin.backend.dto.usernote.CreateUsernoteResponse;
import com.sooktin.backend.repository.UserRepository;
import com.sooktin.backend.service.UsernoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping
public class UsernoteController {

    /**
     * 클라이언트(우리는 리액트)에게 전달합니다.
     *  controller에서 @GetMapping() { } 등등이 쓰임
     **/

    private final UsernoteService usernoteService;
    private final UserRepository userRepository;

    @Autowired
    public UsernoteController(UsernoteService usernoteService, UserRepository userRepository) {
        this.usernoteService = usernoteService;
        this.userRepository = userRepository;
    }

    // 포스트 목록 조회
    @GetMapping
    public ResponseEntity<?> getAllUsernotes() {
        try {
            List<CreateUsernoteResponse> usernotes = usernoteService.findAll().stream()
                    .map(CreateUsernoteResponse::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(usernotes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("게시글 목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 특정 포스트 조회
    @GetMapping("/usernote/{id}")
    public ResponseEntity<?> getUsernoteById(@PathVariable Long id) {
        try {
            Optional<Usernote> usernote = usernoteService.findById(id);
            return usernote.map(note -> ResponseEntity.ok(new CreateUsernoteResponse(note)))
                    .orElseGet(() -> ResponseEntity.notFound().build()); // 아 이거 오류메세지 적으면 오류뜨는거 왜그러지
            // ResponseEntity.badRequest().body("해당 게시글을 찾을 수 없습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("게시글 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 포스트 생성
    /* -> 추후에 파라미터로 @AuthenticationPrincipal UserDetails userDetails 넣고
          user 찾을 때 Optional<User> user = userRepository.findByEmail(userDetails.getUsername())도 고려해주세욤
    from 경민 to 수진
    */
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody CreateUsernoteRequest userNoteRequest,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername()) // 이메일로 검색
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            Usernote newUsernote = new Usernote();
            newUsernote.setTitle(userNoteRequest.getTitle());
            newUsernote.setContent(userNoteRequest.getContent());
            newUsernote.setLikes(Optional.ofNullable(userNoteRequest.getLikes()).orElse(0));
            newUsernote.setUser(user);

            Usernote createdUsernote = usernoteService.createUsernote(newUsernote);
            return ResponseEntity.status(201).body(new CreateUsernoteResponse(createdUsernote));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("게시글 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 포스트 수정
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateUsernote(
            @PathVariable Long id,
            @RequestBody CreateUsernoteRequest userNoteRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            Usernote existingUsernote = usernoteService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

            if (!existingUsernote.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("권한이 없습니다.");
            }

            existingUsernote.setTitle(userNoteRequest.getTitle());
            existingUsernote.setContent(userNoteRequest.getContent());
            existingUsernote.setLikes(Optional.ofNullable(userNoteRequest.getLikes()).orElse(0));

            Usernote updatedUsernote = usernoteService.updateUsernote(id, existingUsernote);
            return ResponseEntity.ok(new CreateUsernoteResponse(updatedUsernote));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("게시글 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }


    // 포스트 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUsernote(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            Usernote existingUsernote = usernoteService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

            if (!existingUsernote.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("권한이 없습니다.");
            }

            usernoteService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("게시글 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
