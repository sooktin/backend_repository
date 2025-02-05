package com.sooktin.backend.service;

import com.sooktin.backend.domain.Comment;
import com.sooktin.backend.domain.Liked;
import com.sooktin.backend.domain.Usernote;
import com.sooktin.backend.dto.liked.LikedResponse;
import com.sooktin.backend.repository.CommentRepository;
import com.sooktin.backend.repository.LikedRepository;
import com.sooktin.backend.repository.UsernoteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class LikedService {

    private final LikedRepository likedRepository;
    private final UsernoteRepository usernoteRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public LikedService(LikedRepository likedRepository,
                       UsernoteRepository usernoteRepository,
                       CommentRepository commentRepository) {
        this.likedRepository = likedRepository;
        this.usernoteRepository = usernoteRepository;
        this.commentRepository = commentRepository;
    }

    // 게시글 좋아요 토글
    public LikedResponse toggleNoteLike(Long noteId, Long userId) {
        Usernote post = usernoteRepository.findById(noteId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        Optional<Liked> existingLike = likedRepository.findByPostAndUser_Id(post, userId);

        if (existingLike.isPresent()) {
            likedRepository.delete(existingLike.get());
            return new LikedResponse(false, "좋아요 취소되었습니다.");
        } else {
            Liked newLike = new Liked();
            newLike.setPost(post);
            likedRepository.save(newLike);
            return new LikedResponse(true, "좋아요 추가되었습니다.");
        }
    }

    // 댓글 좋아요 토글
    public LikedResponse toggleCommentLike(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        Optional<Liked> existingLike = likedRepository.findByCommentAndUser_Id(comment, userId);

        if (existingLike.isPresent()) {
            likedRepository.delete(existingLike.get());
            return new LikedResponse(false, "좋아요 취소되었습니다.");
        } else {
            Liked newLike = new Liked();
            newLike.setComment(comment);
            likedRepository.save(newLike);
            return new LikedResponse(true, "좋아요 추가되었습니다.");
        }
    }

    // 게시글 좋아요 수 조회
    public Long getNoteLikeCount(Long noteId) {
        return likedRepository.countByPostId(noteId);
    }

    // 댓글 좋아요 수 조회
    public Long getCommentLikeCount(Long commentId) {
        return likedRepository.countByCommentId(commentId);
    }

    // 게시글 좋아요 상태 확인
    public boolean isPostLikedByUser(Long noteId, Long userId) {
        return likedRepository.existsByPostIdAndUser_Id(noteId, userId);
    }

    // 댓글 좋아요 상태 확인
    public boolean isCommentLikedByUser(Long commentId, Long userId) {
        return likedRepository.existsByCommentIdAndUser_Id(commentId, userId);
    }
}