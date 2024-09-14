package com.sooktin.backend.controller;

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
    public UsernoteController(UsernoteService UsernoteService) {
        this.usernoteService = UsernoteService;
    }

    // 커뮤니티 목록 조회
    @GetMapping
    public List<Usernote> getAllUsernotes() {
        return usernoteService.findAll();
    }

    // 특정 커뮤니티 조회
    @GetMapping("/{id}")
    public ResponseEntity<Usernote> getUsernoteById(@PathVariable Long id) {
        Optional<Usernote> Usernote = usernoteService.findById(id);
        return Usernote.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 포스트 생성
    @PostMapping
    public Usernote createPost(@RequestBody Usernote Usernote) {
        return usernoteService.save(Usernote);
    }
     /*
    // 커뮤니티 수정
    @PutMapping("/{id}")
    public ResponseEntity<Usernote> updateUsernote(@PathVariable Long id, @RequestBody Usernote UsernoteDetails) {
        Optional<Usernote> updatedUsernote = usernoteService.updateUsernote(id, UsernoteDetails);
        return updatedUsernote.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

      */

    /*
    // 커뮤니티 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsernote(@PathVariable Long id) {
        if (usernoteService.deleteById(id)) { // 리턴값 수정해야 함
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    */


}
