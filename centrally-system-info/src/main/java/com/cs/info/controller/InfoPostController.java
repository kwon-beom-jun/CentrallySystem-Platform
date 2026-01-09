package com.cs.info.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.cs.core.enums.Role;
import com.cs.core.security.CustomUserPrincipal;
import com.cs.info.controller.response.PostListPageResponse;
import com.cs.info.entity.InfoPost;
import com.cs.info.enums.VisibilityScope;
import com.cs.info.service.InfoPostService;
import com.cs.info.util.RoleUtils;

/**
 * 게시글 컨트롤러
 */
@RestController
@RequestMapping
public class InfoPostController {

    @Autowired
    private InfoPostService infoPostService;

    /**
     * 게시글 목록 조회
     */
    @GetMapping("/boards/{boardId}/posts")
    public ResponseEntity<PostListPageResponse> getPosts(
            @PathVariable("boardId") Long boardId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "pinned", defaultValue = "true") boolean pinnedFirst) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<VisibilityScope> allowedScopes = RoleUtils.getAllowedVisibilityScopes(auth.getAuthorities());
        
        PostListPageResponse result = infoPostService.getPosts(boardId, page, size, pinnedFirst, allowedScopes);
        return ResponseEntity.ok(result);
    }

    /**
     * 게시글 단건 조회
     */
    @GetMapping("/posts/{postId}")
    public ResponseEntity<InfoPost> getPost(@PathVariable("postId") Long postId) {
        InfoPost post = infoPostService.getPost(postId);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (!RoleUtils.canAccessVisibilityScope(auth.getAuthorities(), post.getVisibilityScope())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(post);
    }

    /**
     * 게시글 생성
     */
    @PostMapping(value = "/boards/{boardId}/posts", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<InfoPost> createPost(
            @PathVariable("boardId") Long boardId,
            @RequestPart("post") InfoPost post,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserPrincipal principal = (CustomUserPrincipal) auth.getPrincipal();
        
        post.setBoardId(boardId);
        InfoPost saved = infoPostService.createPost(post, principal.getUserId(), principal.getUsername(), files);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * 게시글 수정
     */
    @PatchMapping(value = "/posts/{postId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<InfoPost> updatePost(
            @PathVariable("postId") Long postId,
            @RequestPart("post") InfoPost post,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "deletedFileIds", required = false) List<Long> deletedFileIds) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserPrincipal principal = (CustomUserPrincipal) auth.getPrincipal();
        boolean isAdmin = RoleUtils.isAdmin(auth.getAuthorities());
        
        InfoPost updated = infoPostService.updatePost(postId, post, principal.getUserId(), isAdmin, files, deletedFileIds);
        return ResponseEntity.ok(updated);
    }

    /**
     * 게시글 삭제
     */
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable("postId") Long postId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserPrincipal principal = (CustomUserPrincipal) auth.getPrincipal();
        boolean isAdmin = RoleUtils.isAdmin(auth.getAuthorities());
        
        infoPostService.deletePost(postId, principal.getUserId(), isAdmin);
        return ResponseEntity.noContent().build();
    }

    /**
     * 게시글 고정 설정/해제 (ADMIN)
     */
    @PostMapping("/posts/{postId}/pin")
    public ResponseEntity<Void> pinPost(
            @PathVariable("postId") Long postId,
            @RequestBody Boolean pinned) {
        infoPostService.pinPost(postId, pinned);
        return ResponseEntity.noContent().build();
    }
}

