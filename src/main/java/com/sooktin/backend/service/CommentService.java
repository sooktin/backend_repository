package com.sooktin.backend.service;

import com.sooktin.backend.domain.Comment;
import com.sooktin.backend.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    // 삭제 여부에 상관없이 댓글 조회
    @Transactional(readOnly = true)
    public Optional<Comment> findDeletedOrActiveById(Long id) {
        return commentRepository.findById(id); // 삭제 여부를 무시하고 댓글 반환
    }

    // 대댓글 생성
    @Transactional
    public Comment createReply(Long parentId, Comment reply) {
        Comment parentComment = findDeletedOrActiveById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다."));


        // 대댓글인지 확인
        if (parentComment.getParentId() != null) {
            throw new IllegalArgumentException("대댓글에 대댓글을 작성할 수 없습니다.");
        }

        reply.setParentId(parentId);
        return commentRepository.save(reply);
    }
    /* 모든 조회 기능에서 삭제된 댓글은 필터링됨 */

    // 삭제된 댓글 필터링 메소드
    private boolean isNotDeleted(Comment comment) {
        return !comment.isDeleted();
    }

    // 부모 댓글 id로 대댓글 조회
    @Transactional(readOnly = true)
    public List<Comment> findRepliesByParentId(Long parentId) {
        return commentRepository.findByParentId(parentId); // 부모 댓글 삭제 여부에 관계없이 반환
    }

    // ID로 댓글 조회
    @Transactional(readOnly = true)
    public Optional<Comment> findById(Long id) {
        return commentRepository.findById(id)
                .filter(this::isNotDeleted);
    }

    // 특정 사용자의 모든 댓글 조회
    @Transactional(readOnly = true)
    public List<Comment> findByUserId(Long userId) {
        return commentRepository.findByUserId(userId).stream()
                .filter(this::isNotDeleted)
                .collect(Collectors.toList());
    }

    // 특정 노트의 모든 댓글 조회
    @Transactional(readOnly = true)
    public List<Comment> findByUsernoteId(Long usernoteId) {
        return commentRepository.findByUsernoteId(usernoteId).stream()
                .filter(this::isNotDeleted)
                .collect(Collectors.toList());
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

        if (comment.isDeleted()) {
            throw new IllegalStateException("삭제된 댓글은 수정할 수 없습니다.");
        }

        comment.setContent(updatedComment.getContent());
        //comment.setLikes(updatedComment.getLikes());
        return commentRepository.save(comment);
    }

    // 댓글 삭제 - 실제론 삭제되지 않았음
    @Transactional
    public boolean deleteById(Long id) {
        Optional<Comment> commentOptional = commentRepository.findById(id);
        if (commentOptional.isEmpty()) {
            throw new IllegalArgumentException("해당 댓글이 존재하지 않습니다. id: " + id);
        }

        Comment comment = commentOptional.get();
        if (comment.isDeleted()) {
            throw new IllegalStateException("이미 삭제된 댓글입니다.");
        }

        // 삭제 상태로 표시
        comment.markAsDeleted();
        commentRepository.save(comment);
        return true;
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
