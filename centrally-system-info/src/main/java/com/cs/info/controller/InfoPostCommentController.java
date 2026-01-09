package com.cs.info.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.cs.core.security.CustomUserPrincipal;
import com.cs.info.entity.InfoPostComment;
import com.cs.info.service.InfoPostCommentService;
import com.cs.info.util.RoleUtils;

/**
 * 댓글 컨트롤러
 */
@RestController
@RequestMapping
public class InfoPostCommentController {

    @Autowired
    private InfoPostCommentService infoPostCommentService;

    /**
     * 댓글 목록 조회
     */
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<InfoPostComment>> getComments(@PathVariable("postId") Long postId) {
        List<InfoPostComment> comments = infoPostCommentService.getComments(postId);
        return ResponseEntity.ok(comments);
    }

    /**
     * 댓글 등록
     */
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<InfoPostComment> addComment(
            @PathVariable("postId") Long postId,
            @RequestBody CommentRequest request) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserPrincipal principal = (CustomUserPrincipal) auth.getPrincipal();
        
        InfoPostComment saved = infoPostCommentService.addComment(
                postId,
                principal.getUserId(),
                principal.getUsername(),
                request.content(),
                request.parentCommentId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * 댓글 수정
     */
    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<InfoPostComment> updateComment(
            @PathVariable("commentId") Long commentId,
            @RequestBody UpdateCommentRequest request) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserPrincipal principal = (CustomUserPrincipal) auth.getPrincipal();
        
        InfoPostComment updated = infoPostCommentService.updateComment(commentId, principal.getUserId(), request.content());
        return ResponseEntity.ok(updated);
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable("commentId") Long commentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserPrincipal principal = (CustomUserPrincipal) auth.getPrincipal();
        boolean isAdmin = RoleUtils.isAdmin(auth.getAuthorities());
        
        infoPostCommentService.deleteComment(commentId, principal.getUserId(), isAdmin);
        return ResponseEntity.noContent().build();
    }

    /**
     * 댓글 요청 DTO
     */
    public static record CommentRequest(String content, Long parentCommentId) {}

    /**
     * 댓글 수정 요청 DTO
     */
    public static record UpdateCommentRequest(String content) {}
}

