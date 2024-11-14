package com.sooktin.backend.repository;

import com.sooktin.backend.domain.Liked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikedRepository extends JpaRepository<Liked, Long> {

    // 특정 게시물에 특정 사용자의 좋아요 여부 확인
    boolean existsByPost_idIdAndUserId(Long postId, Long userId);

    // 특정 게시물에 대한 좋아요 개수
    Long countByPost_idId(Long postId);

    // 특정 댓글에 대한 좋아요 개수
    Long countByComment_id(Long commentId);

    Liked findByPost_idIdAndUserId(Long postId, Long userId);
}
