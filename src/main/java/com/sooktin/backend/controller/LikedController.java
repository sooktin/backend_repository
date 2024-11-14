package com.sooktin.backend.controller;

import com.sooktin.backend.service.LikedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class LikedController {

    @Autowired
    private LikedService likedService;

    @PostMapping("/toggle")
    public String toggleLike(@RequestParam Long postId, @RequestParam Long userId) {
        return likedService.toggleLike(postId, userId);
    }

    @GetMapping("/usernotes/{postId}/likes")
    public Long countLikesByPost(@PathVariable Long postId) {
        return likedService.countLikesByPost(postId);
    }

    @GetMapping("/comment/{commentId}/likes")
    public Long countLikesByComment(@PathVariable Long commentId) {
        return likedService.countLikesByComment(commentId);
    }

    //comment와 usernote 여부 확인
    @GetMapping("/exist")
    public boolean existsLikeByUser(@RequestParam Long postId, @RequestParam Long userId) {
        return likedService.existsLikeByUser(postId, userId);
    }
}
