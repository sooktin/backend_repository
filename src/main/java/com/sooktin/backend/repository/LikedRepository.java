package com.sooktin.backend.repository;

import com.sooktin.backend.domain.Comment;
import com.sooktin.backend.domain.Liked;
import com.sooktin.backend.domain.Usernote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikedRepository extends JpaRepository<Liked, Long> {
    // 게시글 좋아요 관련
    Optional<Liked> findByPostAndUser_Id(Usernote post, Long userId);
    boolean existsByPostIdAndUser_Id(Long postId, Long user);
    Long countByPostId(Long postId);

    // 댓글 좋아요 관련
    Optional<Liked> findByCommentAndUser_Id(Comment comment, Long userId);
    boolean existsByCommentIdAndUser_Id(Long commentId, Long userId);
    Long countByCommentId(Long commentId);

    // 특정 사용자의 모든 좋아요 조회
    List<Liked> findAllByUser_Id(Long userId);

    // 게시글/댓글 삭제 시 연관된 좋아요도 삭제하기 위한 메서드
    void deleteAllByPostId(Long postId);
    void deleteAllByCommentId(Long commentId);
}