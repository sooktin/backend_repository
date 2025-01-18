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
import com.sooktin.backend.service.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    // 공통 엔티티 조회 메서드
    private <T> T findEntityById(Optional<T> entityOptional, String errorMessage) {
        return entityOptional.orElseThrow(() -> new IllegalArgumentException(errorMessage));
    }

    // 공통 응답 생성 메서드
    private <T> ResponseEntity<ResponseDto<T>> buildResponse(int statusCode, String message, T data) {
        return ResponseEntity.status(statusCode)
                .body(new ResponseDto<>(statusCode, message, data));
    }

    private void validateOwnership(Comment comment, Long userId) {
        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
    }

    // 댓글이 특정 게시글에 속해 있는지 확인
    private void validateCommentBelongsToNote(Comment comment, Long noteId) {
        if (!comment.getUsernote().getId().equals(noteId)) {
            throw new IllegalArgumentException("해당 댓글이 이 게시글에 속하지 않습니다.");
        }
    }

    // R - 특정 사용자가 작성한 댓글 목록 조회
    @GetMapping("/user")
    public ResponseEntity<ResponseDto<List<CreateCommentResponse>>> getCommentsByUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            if (userDetails == null) {
                return buildResponse(401, "인증 정보가 유효하지 않습니다. 다시 로그인해주세요.", null);
            }

            List<Comment> comments = commentService.findByUserId(userDetails.getUserId());
            if (comments.isEmpty()) {
                return buildResponse(404, "사용자의 댓글이 존재하지 않습니다.", null);
            }

            List<CreateCommentResponse> responseDtos = comments.stream()
                    .map(CreateCommentResponse::new)
                    .collect(Collectors.toList());
            return buildResponse(200, "사용자의 댓글 목록을 불러왔습니다.", responseDtos);
        } catch (Exception e) {
            return buildResponse(500, "사용자의 댓글을 불러오는 중 오류가 발생했습니다.", null);
        }
    }


    // R - 특정 노트에 작성된 댓글 목록 조회
    @GetMapping
    public ResponseEntity<ResponseDto<List<CreateCommentResponse>>> getCommentsByNoteId(
            @PathVariable Long noteId) {
        try {
            List<Comment> comments = commentService.findByUsernoteId(noteId);
            if (comments.isEmpty()) {
                return buildResponse(404, "게시글에 댓글이 존재하지 않습니다.", null);
            }

            List<CreateCommentResponse> responseDtos = comments.stream()
                    .map(CreateCommentResponse::new)
                    .collect(Collectors.toList());
            return buildResponse(200, "게시글의 댓글 목록을 불러왔습니다.", responseDtos);
        } catch (Exception e) {
            return buildResponse(500, "게시글의 댓글을 불러오는 중 오류가 발생했습니다.", null);
        }
    }

    // R - 댓글 ID로 해당 댓글 조회
    @GetMapping("/{commentId}")
    public ResponseEntity<ResponseDto<CreateCommentResponse>> getCommentById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long noteId,
            @PathVariable Long commentId) {
        try {
            if (userDetails == null) {
                return buildResponse(401, "인증 정보가 유효하지 않습니다. 다시 로그인해주세요.", null);
            }

            Comment comment = findEntityById(commentService.findById(commentId), "댓글을 찾을 수 없습니다.");
            validateCommentBelongsToNote(comment, noteId);

            return buildResponse(200, "댓글을 조회했습니다.", new CreateCommentResponse(comment));
        } catch (IllegalArgumentException e) {
            return buildResponse(404, e.getMessage(), null);
        } catch (Exception e) {
            return buildResponse(500, "댓글 조회 중 오류가 발생했습니다.", null);
        }
    }

    // R - 대댓글 조회
    @GetMapping("/{parentId}/replies")
    public ResponseEntity<ResponseDto<List<CreateCommentResponse>>> getReplies(
            @PathVariable Long noteId,
            @PathVariable Long parentId) {
        try {
            // parentId로 대댓글 목록 조회 (삭제된 댓글 제외)
            List<Comment> replies = commentService.findRepliesByParentId(parentId).stream()
                    .filter(reply -> !reply.isDeleted()) // 삭제된 대댓글 제외
                    .toList();

            if (replies.isEmpty()) {
                return buildResponse(404, "대댓글이 존재하지 않습니다.", null);
            }

            List<CreateCommentResponse> responseDtos = replies.stream()
                    .map(CreateCommentResponse::new)
                    .collect(Collectors.toList());

            return buildResponse(200, "대댓글 목록을 불러왔습니다.", responseDtos);
        } catch (Exception e) {
            return buildResponse(500, "대댓글 조회 중 서버 오류가 발생했습니다.", null);
        }
    }

    // C - 댓글 작성
    @PostMapping
    public ResponseEntity<ResponseDto<CreateCommentResponse>> createComment(
            @PathVariable Long noteId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CreateCommentRequest commentRequest) {
        try {
            if (userDetails == null) {
                return buildResponse(401, "인증 정보가 유효하지 않습니다. 다시 로그인해주세요.", null);
            }

            Usernote usernote = usernoteRepository.findById(noteId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            Comment newComment = new Comment();
            newComment.setContent(commentRequest.getContent());
            newComment.setLikes(Optional.ofNullable(commentRequest.getLikes()).orElse(0));
            newComment.setUser(user);
            newComment.setUsernote(usernote);

            Comment createdComment = commentService.createComment(newComment);
            return buildResponse(201, "댓글이 생성되었습니다.", new CreateCommentResponse(createdComment));
        } catch (IllegalArgumentException e) {
            return buildResponse(400, "댓글을 생성하는 중 오류가 발생했습니다: " + e.getMessage(), null);
        } catch (Exception e) {
            return buildResponse(500, "서버 내부 오류가 발생했습니다.", null);
        }
    }

    // C - 대댓글 작성
    @PostMapping("/{parentId}/replies")
    public ResponseEntity<ResponseDto<CreateCommentResponse>> createReply(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long noteId,
            @PathVariable Long parentId,
            @RequestBody CreateCommentRequest replyRequest) {
        try {
            // 인증된 사용자 확인
            if (userDetails == null) {
                return buildResponse(401, "인증 정보가 유효하지 않습니다. 다시 로그인해주세요.", null);
            }

            // 게시글 존재 여부 확인
            Usernote usernote = usernoteRepository.findById(noteId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

            // 사용자 정보 조회
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            // 부모 댓글 존재 여부 확인
            Comment parentComment = commentService.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다."));

            // 대댓글 생성
            Comment reply = new Comment();
            reply.setContent(replyRequest.getContent());
            reply.setLikes(Optional.ofNullable(replyRequest.getLikes()).orElse(0));
            reply.setUser(user);
            reply.setUsernote(usernote);

            Comment createdReply = commentService.createReply(parentId, reply);
            return buildResponse(201, "대댓글이 생성되었습니다.", new CreateCommentResponse(createdReply));
        } catch (IllegalArgumentException e) {
            return buildResponse(400, "대댓글 생성 중 오류가 발생했습니다: " + e.getMessage(), null);
        } catch (Exception e) {
            return buildResponse(500, "대댓글 생성 중 서버 오류가 발생했습니다: " + e.getMessage(), null);
        }
    }

    // U - 댓글 수정
    @PatchMapping("/{commentId}")
    public ResponseEntity<ResponseDto<CreateCommentResponse>> updateComment(
            @PathVariable Long noteId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CreateCommentRequest commentRequest) {
        try {
            if (userDetails == null) {
                return buildResponse(401, "인증 정보가 유효하지 않습니다. 다시 로그인해주세요.", null);
            }

            // 댓글 조회
            Comment comment = commentService.findById(commentId)
                    .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다.")); // 이 경우 404 처리

            validateCommentBelongsToNote(comment, noteId);
            validateOwnership(comment, userDetails.getUserId());

            // 댓글 수정
            comment.setContent(commentRequest.getContent());
            comment.setLikes(Optional.ofNullable(commentRequest.getLikes()).orElse(0));
            Comment updatedComment = commentService.updateComment(commentId, comment);

            return buildResponse(200, "댓글이 수정되었습니다.", new CreateCommentResponse(updatedComment));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals("댓글을 찾을 수 없습니다.")) {
                return buildResponse(404, e.getMessage(), null);
            }
            return buildResponse(400, "댓글을 수정하는 중 오류가 발생했습니다: " + e.getMessage(), null);
        } catch (Exception e) {
            return buildResponse(500, "댓글을 수정하는 중 서버 내부 오류가 발생했습니다.", null);
        }
    }


    // D - 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ResponseDto<Void>> deleteComment(
            @PathVariable Long noteId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            if (userDetails == null) {
                return buildResponse(401, "인증 정보가 유효하지 않습니다. 다시 로그인해주세요.", null);
            }

            Comment comment = commentService.findById(commentId)
                    .orElseThrow(() -> new IllegalStateException("댓글을 찾을 수 없습니다.")); // 404 예외 처리

            if (!comment.getUsernote().getId().equals(noteId)) {
                throw new IllegalArgumentException("해당 댓글이 이 게시글에 속하지 않습니다.");
            }

            validateOwnership(comment, userDetails.getUserId());

            commentService.deleteById(commentId);
            return buildResponse(204, "댓글이 삭제되었습니다.", null);
        } catch (IllegalStateException e) {
            return buildResponse(404, e.getMessage(), null);
        } catch (IllegalArgumentException e) {
            return buildResponse(400, "댓글 삭제 중 오류가 발생했습니다: " + e.getMessage(), null);
        } catch (Exception e) {
            return buildResponse(500, "서버 내부 오류가 발생했습니다.", null);
        }
    }
}