package com.sooktin.backend.controller;

import com.sooktin.backend.domain.User;
import com.sooktin.backend.domain.Usernote;
import com.sooktin.backend.dto.usernote.CreateUsernoteRequest;
import com.sooktin.backend.dto.usernote.CreateUsernoteResponse;
import com.sooktin.backend.repository.UserRepository;
import com.sooktin.backend.service.UsernoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping
public class UsernoteController {

    private final UsernoteService usernoteService;
    private final UserRepository userRepository;

    @Autowired
    public UsernoteController(UsernoteService usernoteService, UserRepository userRepository) {
        this.usernoteService = usernoteService;
        this.userRepository = userRepository;
    }

    // 포스트 목록 조회
    @GetMapping("/usernotes")
    public ResponseEntity<?> getAllUsernotes() {
        try {
            List<Usernote> usernotes = usernoteService.findAll();
            List<CreateUsernoteResponse> usernoteDtos = usernotes.stream()
                    .map(CreateUsernoteResponse::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(usernoteDtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("게시글 목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 특정 포스트 조회
    @GetMapping("/usernote/{id}")
    public ResponseEntity<?> getUsernoteById(@PathVariable Long id) {
        try {
            Optional<Usernote> usernoteOptional = usernoteService.findById(id);
            if (usernoteOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            CreateUsernoteResponse usernoteDto = new CreateUsernoteResponse(usernoteOptional.get());
            return ResponseEntity.ok(usernoteDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("게시글 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 포스트 생성
    @PostMapping("/usernotes")
    public ResponseEntity<?> createPost(@RequestBody CreateUsernoteRequest userNoteRequest) {
        try {
            Optional<User> userOptional = userRepository.findById(userNoteRequest.getUserId());
            if (userOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            User user = userOptional.get();

            Usernote newUsernote = new Usernote();
            newUsernote.setTitle(userNoteRequest.getTitle());
            newUsernote.setContent(userNoteRequest.getContent());
            newUsernote.setLikes(userNoteRequest.getLikes() != null ? userNoteRequest.getLikes() : 0);
            newUsernote.setUser(user);

            Usernote createdUsernote = usernoteService.createUsernote(newUsernote);
            CreateUsernoteResponse usernoteDto = new CreateUsernoteResponse(createdUsernote);

            return ResponseEntity.status(201).body(usernoteDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("게시글 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 포스트 수정
    @PatchMapping("/usernote/{id}")
    public ResponseEntity<?> updateUsernote(@PathVariable Long id, @RequestBody Usernote usernoteDetails) {
        try {
            Usernote updatedUsernote = usernoteService.updateUsernote(id, usernoteDetails);
            CreateUsernoteResponse usernoteDto = new CreateUsernoteResponse(updatedUsernote);
            return ResponseEntity.ok(usernoteDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("게시글 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 포스트 삭제
    @DeleteMapping("/usernote/{id}")
    public ResponseEntity<?> deleteUsernote(@PathVariable Long id) {
        try {
            if (usernoteService.deleteById(id)) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.badRequest().body("해당 게시글을 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("게시글 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}