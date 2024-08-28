package com.sooktin.backend.controller;

import com.sooktin.backend.domain.Community;
import com.sooktin.backend.service.CommunityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping
public class CommunityController {

    /**
     * 클라이언트(우리는 리액트)에게 전달합니다.
     *  controller에서 @GetMapping() { } 등등이 쓰임
     **/

    private final CommunityService communityService;

    @Autowired
    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }



    // 커뮤니티 목록 조회
    @GetMapping
    public List<Community> getAllCommunities() {
        return communityService.findAll();
    }

    // 특정 커뮤니티 조회
    @GetMapping("/{id}")
    public ResponseEntity<Community> getCommunityById(@PathVariable Long id) {
        Optional<Community> community = communityService.findById(id);
        return community.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 포스트 생성
    @PostMapping
    public Community createPost(@RequestBody Community community) {
        return communityService.save(community);
    }

    // 커뮤니티 수정
    @PutMapping("/{id}")
    public ResponseEntity<Community> updateCommunity(@PathVariable Long id, @RequestBody Community communityDetails) {
        Optional<Community> updatedCommunity = communityService.updateCommunity(id, communityDetails);
        return updatedCommunity.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 커뮤니티 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCommunity(@PathVariable Long id) {
        if (communityService.deleteById(id)) { // 리턴값 수정해야 함
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
