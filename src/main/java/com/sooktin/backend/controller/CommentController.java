package com.sooktin.backend.controller;

import com.sooktin.backend.domain.Comment;
import com.sooktin.backend.domain.User;
import com.sooktin.backend.domain.Usernote;
import com.sooktin.backend.dto.ResponseDto;
import com.sooktin.backend.dto.comment.CreateCommentRequest;
import com.sooktin.backend.dto.comment.CreateCommentResponse;
import com.sooktin.backend.repository.UserRepository;
import com.sooktin.backend.repository.UsernoteRepository;
import com.sooktin.backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usernote/{noteId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final UserRepository userRepository;
    private final UsernoteRepository usernoteRepository;

    @Autowired
    public CommentController(CommentService commentService, UserRepository userRepository, UsernoteRepository usernoteRepository) {
        this.commentService = commentService;
        this.userRepository = userRepository;
        this.usernoteRepository = usernoteRepository;
    }

    // R - 특정 사용자가 작성한 댓글 목록 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseDto<List<CreateCommentResponse>>> getCommentsByUserId(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable Long userId) {
        try {
            List<Comment> comments = commentService.findByUserId(userId);
            if (comments.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(new ResponseDto<>(404, "사용자의 댓글이 존재하지 않습니다.", null));
            }

            List<CreateCommentResponse> responseDtos = comments.stream()
                    .map(CreateCommentResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.status(200)
                    .body(new ResponseDto<>(200, "사용자의 댓글 목록을 불러왔습니다.", responseDtos));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(new ResponseDto<>(400, "사용자의 댓글을 불러오는 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }

    // R - 특정 노트에 작성된 댓글 목록 조회
    @GetMapping
    public ResponseEntity<ResponseDto<List<CreateCommentResponse>>> getCommentsByNoteId(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable Long noteId) {
        try {
            List<Comment> comments = commentService.findByUsernoteId(noteId);
            if (comments.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(new ResponseDto<>(404, "게시글에 댓글이 존재하지 않습니다.", null));
            }

            List<CreateCommentResponse> responseDtos = comments.stream()
                    .map(CreateCommentResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.status(200)
                    .body(new ResponseDto<>(200, "게시글의 댓글 목록을 불러왔습니다.", responseDtos));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(new ResponseDto<>(400, "게시글의 댓글을 불러오는 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }

    // R - 댓글 ID로 해당 댓글 조회
    @GetMapping("/{commentId}")
    public ResponseEntity<ResponseDto<CreateCommentResponse>> getCommentById(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable Long noteId,
            @PathVariable Long commentId) {
        try {
            Optional<Comment> commentOptional = commentService.findById(commentId);
            if (commentOptional.isEmpty() || !commentOptional.get().getUsernote().getId().equals(noteId)) {
                return ResponseEntity.status(404)
                        .body(new ResponseDto<>(404, "댓글을 찾을 수 없습니다.", null));
            }

            CreateCommentResponse responseDto = new CreateCommentResponse(commentOptional.get());
            return ResponseEntity.status(200)
                    .body(new ResponseDto<>(200, "댓글을 조회했습니다.", responseDto));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(new ResponseDto<>(400, "댓글을 조회하는 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }

    // R - 대댓글 조회
    @GetMapping("/{parentId}/replies")
    public ResponseEntity<ResponseDto<List<CreateCommentResponse>>> getReplies(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable Long noteId,
            @PathVariable Long parentId) {
        try {
            List<Comment> replies = commentService.findRepliesByParentId(parentId);
            if (replies.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(new ResponseDto<>(404, "대댓글이 존재하지 않습니다.", null));
            }

            List<CreateCommentResponse> responseDtos = replies.stream()
                    .map(CreateCommentResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.status(200)
                    .body(new ResponseDto<>(200, "대댓글 목록을 불러왔습니다.", responseDtos));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(new ResponseDto<>(400, "대댓글 목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }

    // C - 댓글 작성
    @PostMapping
    public ResponseEntity<ResponseDto<CreateCommentResponse>> createComment(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable Long noteId,
            @RequestBody CreateCommentRequest commentRequest) {
        try {
            Optional<Usernote> usernoteOptional = usernoteRepository.findById(noteId);
            if (usernoteOptional.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(new ResponseDto<>(404, "해당 게시글을 찾을 수 없습니다.", null));
            }

            Optional<User> userOptional = userRepository.findById(commentRequest.getUserId());
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(new ResponseDto<>(404, "해당 유저를 찾을 수 없습니다.", null));
            }

            Comment newComment = new Comment();
            newComment.setContent(commentRequest.getContent());
            newComment.setLikes(commentRequest.getLikes() != null ? commentRequest.getLikes() : 0);
            newComment.setUser(userOptional.get());
            newComment.setUsernote(usernoteOptional.get());

            Comment createdComment = commentService.createComment(newComment);

            CreateCommentResponse responseDto = new CreateCommentResponse(createdComment);
            return ResponseEntity.status(201)
                    .body(new ResponseDto<>(201, "댓글이 생성되었습니다.", responseDto));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(new ResponseDto<>(400, "댓글을 생성하는 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }

    // C - 대댓글 작성
    @PostMapping("/{parentId}/replies")
    public ResponseEntity<ResponseDto<CreateCommentResponse>> createReply(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable Long noteId,
            @PathVariable Long parentId,
            @RequestBody CreateCommentRequest replyRequest) {
        try {
            // 게시글 확인
            Optional<Usernote> usernoteOptional = usernoteRepository.findById(noteId);
            if (usernoteOptional.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(new ResponseDto<>(404, "해당 게시글을 찾을 수 없습니다.", null));
            }

            // 유저 확인
            Optional<User> userOptional = userRepository.findById(replyRequest.getUserId());
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(new ResponseDto<>(404, "해당 유저를 찾을 수 없습니다.", null));
            }

            // 부모 댓글 확인
            Optional<Comment> parentCommentOptional = commentService.findById(parentId);
            if (parentCommentOptional.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(new ResponseDto<>(404, "부모 댓글을 찾을 수 없습니다.", null));
            }

            // 대댓글 생성
            Comment reply = new Comment();
            reply.setContent(replyRequest.getContent());
            reply.setLikes(replyRequest.getLikes() != null ? replyRequest.getLikes() : 0);
            reply.setUser(userOptional.get());
            reply.setUsernote(usernoteOptional.get());

            Comment createdReply = commentService.createReply(parentId, reply);

            CreateCommentResponse responseDto = new CreateCommentResponse(createdReply);

            return ResponseEntity.status(201)
                    .body(new ResponseDto<>(201, "대댓글이 생성되었습니다.", responseDto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                    .body(new ResponseDto<>(400, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(new ResponseDto<>(400, "대댓글을 생성하는 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }

    // U - 댓글 수정
    @PatchMapping("/{commentId}")
    public ResponseEntity<ResponseDto<CreateCommentResponse>> updateComment(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable Long noteId,
            @PathVariable Long commentId,
            @RequestBody CreateCommentRequest commentRequest) {
        try {
            Optional<Comment> commentOptional = commentService.findById(commentId);
            if (commentOptional.isEmpty() || !commentOptional.get().getUsernote().getId().equals(noteId)) {
                return ResponseEntity.status(404)
                        .body(new ResponseDto<>(404, "댓글을 찾을 수 없습니다.", null));
            }

            Comment updatedComment = commentOptional.get();
            updatedComment.setContent(commentRequest.getContent());
            if (commentRequest.getLikes() != null) {
                updatedComment.setLikes(commentRequest.getLikes());
            }

            Comment result = commentService.updateComment(commentId, updatedComment);
            CreateCommentResponse responseDto = new CreateCommentResponse(result);
            return ResponseEntity.status(200)
                    .body(new ResponseDto<>(200, "댓글이 수정되었습니다.", responseDto));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(new ResponseDto<>(400, "댓글을 수정하는 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }

    // D - 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ResponseDto<Void>> deleteComment(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable Long noteId,
            @PathVariable Long commentId) {
        try {
            Optional<Comment> commentOptional = commentService.findById(commentId);
            if (commentOptional.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(new ResponseDto<>(404, "댓글을 찾을 수 없습니다.", null));
            }

            commentService.deleteById(commentId);
            return ResponseEntity.status(204).build();
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(new ResponseDto<>(400, "댓글을 삭제하는 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }
}