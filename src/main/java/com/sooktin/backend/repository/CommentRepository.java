package com.sooktin.backend.repository;

import com.sooktin.backend.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Optional<Comment> findById(Long id);

    // 특정 사용자(user_id)의 모든 댓글 조회
    List<Comment> findByUserId(Long userId);

    // 특정 노트(usernote_id)에 대한 모든 댓글 조회
    List<Comment> findByUsernoteId(Long usernoteId);

    // 특정 노트에 특정 사용자가 단 댓글 조회
    Optional<Comment> findByUserIdAndUsernoteId(Long userId, Long usernoteId);

    // 내용으로 댓글 검색
    List<Comment> findByContent(String keyword);
}
