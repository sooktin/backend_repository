package com.sooktin.backend.service;

import com.sooktin.backend.domain.Liked;
import com.sooktin.backend.domain.Usernote;
import com.sooktin.backend.repository.LikedRepository;
import com.sooktin.backend.repository.UsernoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

@Service
public class LikedService {

    @Autowired
    private LikedRepository likedRepository;

    @Autowired
    private UsernoteRepository usernoteRepository;

    @Transactional
    public String toggleLike(Long postId, Long userId) {
        Usernote post = usernoteRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Invalid post ID"));

        if (likedRepository.existsByPost_idIdAndUserId(postId, userId)) {
            Liked liked = likedRepository.findByPost_idIdAndUserId(postId, userId);
            likedRepository.delete(liked);
            return "좋아요 취소";
        } else {
            Liked liked = new Liked();
            liked.setPost_id(post); // Usernote 객체 설정
            liked.setUserId(userId);
            likedRepository.save(liked);
            return "좋아요 추가";
        }
    }

    public Long countLikesByPost(Long postId) {
        return likedRepository.countByPost_idId(postId);
    }

    public Long countLikesByComment(Long commentId) {
        return likedRepository.countByComment_id(commentId);
    }

    public boolean existsLikeByUser(Long postId, Long userId) {
        return likedRepository.existsByPost_idIdAndUserId(postId, userId);
    }
}

//setPost_id(postId) 부분에서 오류가 발생하는 이유는 Liked 엔티티의 post_id 필드가 Usernote 엔티티와의 연관 관계로 정의되어 있기 때문입니다. post_id는 단순한 Long 타입이 아니라 Usernote 타입이어야 합니다. 따라서, Liked 엔티티에서 post_id 대신 Usernote 객체를 설정해야 합니다.