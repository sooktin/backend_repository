package com.sooktin.backend.repository;

import com.sooktin.backend.domain.Comment;
import com.sooktin.backend.domain.Liked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

//comments import 필요

@Repository
public interface LikedRepository extends JpaRepository<Liked, Long> {

    // 특정 게시물에 특정 사용자의 좋아요 여부 확인
    boolean existsByPostAndUserId(Long postId, Long userId);
    // 특정 댓글에 특정 사용자의 좋아요 여부 확인
    boolean existsByCommentAndUserId(Comment comment, Long userId);

    // 특정 게시물에 대한 좋아요 개수
    Long countByPost(Long postId);
    // 특정 댓글에 대한 좋아요 개수
    Long countByComment(Long commentId);

    //findBy-----
    Liked findByPostAndUserId(Long postId, Long userId);

    // 특정 게시물에 대해 좋아요를 누른 사용자 목록 조회
    List<Liked> findByPost(Long postId);
    // 특정 댓글에 대해 좋아요를 누른 사용자 목록 조회
    List<Liked> findByComment(Long commentId);

    Liked findByCommentAndUserId(Comment comment, Long userId);
}
