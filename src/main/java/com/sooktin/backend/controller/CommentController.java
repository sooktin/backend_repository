package com.sooktin.backend.controller;

import com.sooktin.backend.domain.Comment;
import com.sooktin.backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usernote/{noteId}/comments")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // 특정 사용자(userId)의 모든 댓글 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getCommentsByUserId(@PathVariable Long userId) {
        try {
            List<Comment> comments = commentService.findByUserId(userId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("사용자의 댓글을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 특정 게시글에 대한 모든 댓글 조회
    @GetMapping
    public ResponseEntity<?> getCommentsByNoteId(@PathVariable Long noteId) {
        try {
            List<Comment> comments = commentService.findByUsernoteId(noteId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("게시글의 댓글을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 특정 게시글 내 특정 댓글 조회
    @GetMapping("/{commentId}")
    public ResponseEntity<?> getCommentById(@PathVariable Long noteId, @PathVariable Long commentId) {
        try {
            Optional<Comment> comment = commentService.findById(commentId);
            // comment 객체가 있고, 해당 코멘트가 달린 노트와 params에 입력한 노트가 동일하다면
            if (comment.isPresent() && comment.get().getUsernote().getId().equals(noteId)) {
                return ResponseEntity.ok(comment.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("댓글을 조회하는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 특정 댓글의 대댓글 목록 조회
    @GetMapping("/{parentId}/replies")
    public ResponseEntity<?> getReplies(@PathVariable Long parentId) {
        try {
            List<Comment> replies = commentService.getReplies(parentId);
            return ResponseEntity.ok(replies);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("대댓글 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }


    // 특정 게시글에 댓글 생성
    @PostMapping
    public ResponseEntity<?> createComment(@PathVariable Long noteId, @RequestBody Comment comment) {
        try {
            // 게시글에 속하는 댓글로 설정
            comment.getUsernote().setId(noteId);
            Comment createdComment = commentService.createComment(comment);
            return ResponseEntity.ok(createdComment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("댓글을 생성하는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 특정 댓글의 대댓글 생성
    @PostMapping("/{parentId}/reply")
    public ResponseEntity<?> createReply(@PathVariable Long parentId, @RequestBody Comment reply) {
        try {
            Comment createdReply = commentService.createReply(parentId, reply);
            return ResponseEntity.ok(createdReply);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("대댓글 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 특정 게시글 내 특정 댓글 수정
    @PatchMapping("/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable Long noteId, @PathVariable Long commentId, @RequestBody Comment commentDetails) {
        try {
            Optional<Comment> comment = commentService.findById(commentId);
            if (comment.isPresent() && comment.get().getUsernote().getId().equals(noteId)) {
                Comment updatedComment = commentService.updateComment(commentId, commentDetails);
                return ResponseEntity.ok(updatedComment);
            } else {
                return ResponseEntity.status(403).body("해당 게시글의 댓글만 수정할 수 있습니다.");
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body("해당 댓글을 수정할 권한이 없습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("댓글을 수정하는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 특정 게시글 내 특정 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long noteId, @PathVariable Long commentId) {
        try {
            Optional<Comment> comment = commentService.findById(commentId);
            if (comment.isPresent() && comment.get().getUsernote().getId().equals(noteId)) {
                if (commentService.deleteById(commentId)) {
                    return ResponseEntity.noContent().build();
                }
            }
            return ResponseEntity.badRequest().body("해당 게시글의 댓글만 삭제할 수 있습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("댓글을 삭제하는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
