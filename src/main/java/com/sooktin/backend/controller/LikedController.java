package com.sooktin.backend.controller;

import com.sooktin.backend.service.LikedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/likes")
public class LikedController {

    @Autowired
    private LikedService likedService;

    // /likes/toggle 엔드포인트에서 togglelike 수행
    @PostMapping("/toggle")
    public String toggleLike(@RequestParam(required = false) Long postId,
                             @RequestParam(required = false) Long commentId,
                             @RequestParam Long userId) {
        if (postId == null && commentId == null) {
            throw new IllegalArgumentException("Either postId or commentId must be provided.");
        }
        return likedService.toggleLike(postId, commentId, userId);
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
    public boolean existsLikeByUser(@RequestParam(required = false) Long postId,
                                    @RequestParam(required = false) Long commentId,
                                    @RequestParam Long userId) {
        if (postId == null && commentId == null) {
            throw new IllegalArgumentException("Either postId or commentId must be provided.");
        }
        return likedService.existsLikeByUser(postId, commentId, userId);
    }

}
