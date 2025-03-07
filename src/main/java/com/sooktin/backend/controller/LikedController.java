package com.sooktin.backend.controller;

import com.sooktin.backend.dto.liked.LikedResponse;
import com.sooktin.backend.service.CustomUserDetails;
import com.sooktin.backend.service.LikedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/likes")
public class LikedController {

    private final LikedService likedService;

    @Autowired
    public LikedController(LikedService likedService) {
        this.likedService = likedService;
    }

    // 게시글 좋아요/취소
    @PostMapping("/usernotes/{noteId}")
    public ResponseEntity<LikedResponse> toggleNoteLike(
            @PathVariable Long noteId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        /*if (userDetails == null) {
            return ResponseEntity.status(401).body(LikedResponse.unauthorized());
        }*/
        LikedResponse response = likedService.toggleNoteLike(noteId, userDetails.getUserId());
        return ResponseEntity.ok(response);
    }

    // 댓글 좋아요/취소
    @PostMapping("/comments/{commentId}")
    public ResponseEntity<LikedResponse> toggleCommentLike(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
//        if (userDetails == null) {
//            return ResponseEntity.status(401).body(LikedResponse.unauthorized());
//        }
        LikedResponse response = likedService.toggleCommentLike(commentId, userDetails.getUserId());
        return ResponseEntity.ok(response);
    }

    // 게시글의 좋아요 수 조회
    @GetMapping("/usernotes/{noteId}/count")
    public ResponseEntity<Long> getNoteLikeCount(@PathVariable Long noteId) {
        Long count = likedService.getNoteLikeCount(noteId);
        return ResponseEntity.ok(count);
    }

    // 댓글의 좋아요 수 조회
    @GetMapping("/comments/{commentId}/count")
    public ResponseEntity<Long> getCommentLikeCount(@PathVariable Long commentId) {
        Long count = likedService.getCommentLikeCount(commentId);
        return ResponseEntity.ok(count);
    }

    // 사용자가 게시글에 좋아요를 눌렀는지 확인
    @GetMapping("/usernotes/{noteId}/status")
    public ResponseEntity<Boolean> checkNoteLikeStatus(
            @PathVariable Long noteId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        boolean isLiked = likedService.isPostLikedByUser(noteId, userDetails.getUserId());
        return ResponseEntity.ok(isLiked);
    }

    // 사용자가 댓글에 좋아요를 눌렀는지 확인
    @GetMapping("/comments/{commentId}/status")
    public ResponseEntity<Boolean> checkCommentLikeStatus(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        boolean isLiked = likedService.isCommentLikedByUser(commentId, userDetails.getUserId());
        return ResponseEntity.ok(isLiked);
    }
}