package com.sooktin.backend.service;

import com.sooktin.backend.domain.Comment;
import com.sooktin.backend.domain.Liked;
import com.sooktin.backend.domain.User;
import com.sooktin.backend.domain.Usernote;
import com.sooktin.backend.repository.CommentRepository;
import com.sooktin.backend.repository.LikedRepository;
import com.sooktin.backend.repository.UserRepository;
import com.sooktin.backend.repository.UsernoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;

//comments import 필요

@Service
public class LikedService {

    @Autowired
    private LikedRepository likedRepository;

    @Autowired
    private UsernoteRepository usernoteRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    // C & D - 좋아요 및 좋아요 취소 - 토글 사용
    @Transactional
    public String toggleLike(Long postId, Long commentId, Long userId) {
        // 게시물에 대한 좋아요 처리
        if (postId != null) {
            Usernote post = usernoteRepository.findById(postId)
                    .orElseThrow(() -> new EntityNotFoundException("Invalid post ID"));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Invalid user ID"));

            if (likedRepository.existsByPostAndUserId(post.getId(), userId)) {
                Liked liked = likedRepository.findByPostAndUserId(post.getId(), userId);
                likedRepository.delete(liked);
                return "좋아요 취소";
            } else {
                Liked liked = new Liked();
                liked.setPost_id(post); // Usernote 객체 설정
                liked.setUser(user);   // User 객체 설정
                likedRepository.save(liked);
                return "좋아요 추가";
            }
        }
        // 댓글에 대한 좋아요 처리
        else if (commentId != null) {
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new EntityNotFoundException("Invalid comment ID"));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Invalid user ID"));

            if (likedRepository.existsByCommentAndUserId(comment, userId)) {
                Liked liked = likedRepository.findByCommentAndUserId(comment, userId);
                likedRepository.delete(liked);
                return "좋아요 취소";
            } else {
                Liked liked = new Liked();
                liked.setComment_id(comment); // Comments 객체 설정
                liked.setUser(user);          // User 객체 설정
                likedRepository.save(liked);
                return "좋아요 추가";
            }
        } else {
            throw new IllegalArgumentException("Either postId or commentId must be provided.");
        }
    }



    // R - 특정 게시물에 대해 좋아요를 누른 사용자 목록 조회
    public List<Liked> getLikesByPost(Long postId) {
        return likedRepository.findByPost(postId);
    }

    // R - 특정 댓글에 대해 좋아요를 누른 사용자 목록 조회
    public List<Liked> getLikesByComment(Long commentId) {
        return likedRepository.findByComment(commentId);
    }

    // R - 특정 게시글에 대한 좋아요 개수 반환
    public Long countLikesByPost(Long postId) {
        return likedRepository.countByPost(postId);
    }

    // R - 특정 댓글에 대한 좋아요 개수 반환
    public Long countLikesByComment(Long commentId) {
        return likedRepository.countByComment(commentId);
    }

    // R - 특정 게시글에 특정 사용자가 좋아요를 눌렀는지 여부
    public boolean existsLikeByUser(Long postId, Long userId, Long id) {
        return likedRepository.existsByPostAndUserId(postId, userId);
    }
}