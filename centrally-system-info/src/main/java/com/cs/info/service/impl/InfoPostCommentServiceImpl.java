package com.cs.info.service.impl;

import com.cs.core.handler.GlobalExceptionHandler;
import com.cs.info.entity.InfoPost;
import com.cs.info.entity.InfoPostComment;
import com.cs.info.repository.InfoPostCommentRepository;
import com.cs.info.repository.InfoPostRepository;
import com.cs.info.service.InfoPostCommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 댓글 서비스 구현체
 */
@Service
@Slf4j
public class InfoPostCommentServiceImpl implements InfoPostCommentService {

    @Autowired
    private InfoPostCommentRepository infoPostCommentRepository;

    @Autowired
    private InfoPostRepository infoPostRepository;

    /**
     * 댓글 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<InfoPostComment> getComments(Long postId) {
        InfoPost post = infoPostRepository.findById(postId).orElse(null);
        if (post == null) {
            return List.of();
        }
        return infoPostCommentRepository.findByPostOrderByRegTimeAsc(post);
    }

    /**
     * 댓글 추가 (2depth 제한)
     */
    @Override
    @Transactional
    public InfoPostComment addComment(Long postId, Integer writerId, String writerName, String content, Long parentCommentId) {
        InfoPost post = infoPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "게시글을 찾을 수 없습니다: " + postId));

        InfoPostComment parent = null;
        if (parentCommentId != null) {
            parent = infoPostCommentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "부모 댓글을 찾을 수 없습니다: " + parentCommentId));
            
            if (!parent.getPost().getId().equals(post.getId())) {
                throw new IllegalArgumentException(GlobalExceptionHandler.CC + "부모 댓글이 다른 게시글에 속합니다.");
            }
            
            if (parent.getParent() != null) {
                throw new IllegalArgumentException(GlobalExceptionHandler.CC + "댓글은 최대 2단계까지만 허용됩니다.");
            }
        }

        InfoPostComment comment = InfoPostComment.builder()
                .post(post)
                .parent(parent)
                .writerId(writerId)
                .writerName(writerName)
                .content(content)
                .build();

        return infoPostCommentRepository.save(comment);
    }

    /**
     * 댓글 수정
     */
    @Override
    @Transactional
    public InfoPostComment updateComment(Long commentId, Integer editorUserId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException(GlobalExceptionHandler.CC + "댓글 내용은 비어 있을 수 없습니다.");
        }
        
        InfoPostComment comment = infoPostCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "댓글을 찾을 수 없습니다: " + commentId));
        
        if (!comment.getWriterId().equals(editorUserId)) {
            throw new IllegalArgumentException(GlobalExceptionHandler.CC + "본인만 댓글을 수정할 수 있습니다.");
        }
        
        comment.setContent(content);
        return infoPostCommentRepository.save(comment);
    }

    /**
     * 댓글 삭제 (자식 댓글 포함, 권한 검증 필요)
     */
    @Override
    @Transactional
    public void deleteComment(Long commentId, Integer userId, boolean isAdmin) {
        InfoPostComment comment = infoPostCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "댓글을 찾을 수 없습니다: " + commentId));
        
        // 권한 검증: 작성자 또는 ADMIN만 삭제 가능
        if (!comment.getWriterId().equals(userId) && !isAdmin) {
            throw new IllegalArgumentException(GlobalExceptionHandler.CC + "댓글을 삭제할 권한이 없습니다.");
        }
        
        List<InfoPostComment> children = infoPostCommentRepository.findByParent(comment);
        if (children != null && !children.isEmpty()) {
            for (InfoPostComment child : children) {
                infoPostCommentRepository.delete(child);
            }
        }
        
        infoPostCommentRepository.delete(comment);
    }
}

