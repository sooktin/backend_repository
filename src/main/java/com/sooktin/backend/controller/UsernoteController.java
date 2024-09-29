package com.sooktin.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import com.sooktin.backend.domain.Usernote;
import com.sooktin.backend.service.UsernoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping
public class UsernoteController {

    /**
     * 클라이언트(우리는 리액트)에게 전달합니다.
     *  controller에서 @GetMapping() { } 등등이 쓰임
     **/

    private final UsernoteService usernoteService;

    @Autowired
    public UsernoteController(UsernoteService usernoteService) {
        this.usernoteService = usernoteService;
    }

    // 포스트 목록 조회
    @GetMapping("/usernotes")
    public ResponseEntity<?> getAllUsernotes() {
        try {
            List<Usernote> usernotes = usernoteService.findAll();
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
            return usernote.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build()); // 아 이거 오류메세지 적으면 오류뜨는거 왜그러지
            // ResponseEntity.badRequest().body("해당 게시글을 찾을 수 없습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("게시글 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 포스트 생성
    @PostMapping("/usernotes")
    public ResponseEntity<?> createPost(@RequestBody Usernote usernote) {
        try {
            Usernote newUsernote = new Usernote();
            newUsernote.setTitle(usernote.getTitle());
            newUsernote.setContent(usernote.getContent());
            Usernote createdUsernote = usernoteService.createUsernote(newUsernote);
            return ResponseEntity.ok(createdUsernote);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("게시글 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 포스트 수정
    @PatchMapping("/usernote/{id}")
    public ResponseEntity<?> updateUsernote(@PathVariable Long noteid, @RequestBody Usernote usernoteDetails) {
        try {
            Usernote updatedUsernote = usernoteService.updateUsernote(noteid, usernoteDetails);
            return ResponseEntity.ok(updatedUsernote);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("게시글 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 포스트 삭제
    @DeleteMapping("/usernote/{id}")
    public ResponseEntity<?> deleteUsernote(@PathVariable Long id) {
        try {
            if (usernoteService.deleteById(id)) { // 삭제 성공
                return ResponseEntity.noContent().build();
            } else { // 삭제 실패
                // return ResponseEntity.notFound().build();
                return ResponseEntity.badRequest().body("해당 게시글을 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("게시글 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
