package com.sooktin.backend.service;

import com.sooktin.backend.domain.Comment;
import com.sooktin.backend.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    // 댓글 생성
    public Comment createComment(Comment comment) {
        validateComment(comment);
        return commentRepository.save(comment);
    }

    // 대댓글 생성
    @Transactional
    public Comment createReply(Long parentId, Comment reply) {
        Comment parentComment = commentRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글이 존재하지 않습니다."));

        reply.setParent(parentComment); // 부모 댓글 설정
        reply.setUsernote(parentComment.getUsernote()); // 동일한 게시글에 속하도록 설정
        return commentRepository.save(reply); // 대댓글 저장
    }

    // ID로 댓글 조회
    @Transactional(readOnly = true)
    public Optional<Comment> findById(Long id) {
        return commentRepository.findById(id);
    }

    // 특정 사용자의 모든 댓글 조회
    @Transactional(readOnly = true)
    public List<Comment> findByUserId(Long userId) {
        return commentRepository.findByUserId(userId);
    }

    // 특정 노트의 모든 댓글 조회
    @Transactional(readOnly = true)
    public List<Comment> findByUsernoteId(Long usernoteId) {
        return commentRepository.findByUsernoteId(usernoteId);
    }

    // 특정 사용자와 특정 노트에 대한 댓글 조회
    @Transactional(readOnly = true)
    public Optional<Comment> findByUserIdAndUsernoteId(Long userId, Long usernoteId) {
        return commentRepository.findByUserIdAndUsernoteId(userId, usernoteId);
    }

    // 특정 댓글의 대댓글 목록 조회
    @Transactional(readOnly = true)
    public List<Comment> getReplies(Long parentId) {
        Comment parentComment = commentRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글이 존재하지 않습니다."));
        return parentComment.getReplies();
    }

    // 댓글 업데이트
    @Transactional
    public Comment updateComment(Long id, Comment updatedComment) {
        if (!isOwner(id)) {
            throw new IllegalStateException("본인이 아닌 사용자는 이 댓글을 수정할 수 없습니다.");
        }
        Comment comment = commentRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다. id: " + id)
        );

        comment.setContent(updatedComment.getContent());
        return commentRepository.save(comment);
    }

    // 댓글 삭제
    public boolean deleteById(Long id) {
        if (commentRepository.existsById(id)) {
            if (!isOwner(id)) {
                throw new IllegalStateException("본인이 아닌 사용자는 이 댓글을 삭제할 수 없습니다.");
            }
            commentRepository.deleteById(id);
            return true;
        } else {
            throw new IllegalArgumentException("해당 댓글이 존재하지 않습니다. id: " + id);
        }
    }

    // 유효성 검증 메소드
    public void validateComment(Comment comment) {
        if (comment.getContent() == null || comment.getContent().isEmpty()) {
            throw new IllegalArgumentException("댓글 내용은 비워둘 수 없습니다.");
        }
        if (comment.getContent().length() > 300) {
            throw new IllegalArgumentException("댓글 내용은 최대 300자까지 입력 가능합니다.");
        }
    }

    // 본인 여부 확인 메소드 (임시 구현)
    public boolean isOwner(Long commentId) {
        // 로그인된 사용자의 정보와 비교하는 코드로 구현 가능
        /* 예시 코드:
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다. id: " + commentId)
        );
        return comment.getUser().getId().equals(loggedInUser.getId());
        */
        return true; // 실제 구현 시, 인증 정보 활용 필요
    }
}
