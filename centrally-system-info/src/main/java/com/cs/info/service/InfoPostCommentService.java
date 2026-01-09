package com.cs.info.service;

import com.cs.info.entity.InfoPostComment;

import java.util.List;

/**
 * 댓글 서비스 인터페이스
 */
public interface InfoPostCommentService {

    /**
     * 댓글 목록 조회
     * @param postId 게시글 ID
     * @return 댓글 목록
     */
    List<InfoPostComment> getComments(Long postId);

    /**
     * 댓글 추가 (2depth 제한)
     * @param postId 게시글 ID
     * @param writerId 작성자 ID
     * @param writerName 작성자 이름
     * @param content 댓글 내용
     * @param parentCommentId 부모 댓글 ID (null 가능)
     * @return 생성된 댓글 엔티티
     */
    InfoPostComment addComment(Long postId, Integer writerId, String writerName, String content, Long parentCommentId);

    /**
     * 댓글 수정
     * @param commentId 댓글 ID
     * @param editorUserId 수정자 ID
     * @param content 댓글 내용
     * @return 수정된 댓글 엔티티
     */
    InfoPostComment updateComment(Long commentId, Integer editorUserId, String content);

    /**
     * 댓글 삭제 (자식 댓글 포함, 권한 검증 필요)
     * @param commentId 댓글 ID
     * @param userId 사용자 ID
     * @param isAdmin 관리자 여부
     */
    void deleteComment(Long commentId, Integer userId, boolean isAdmin);
}
