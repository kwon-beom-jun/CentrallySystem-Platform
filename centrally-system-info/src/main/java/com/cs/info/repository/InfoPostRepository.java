package com.cs.info.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cs.info.controller.response.PostListResponse;
import com.cs.info.entity.InfoPost;
import com.cs.info.enums.VisibilityScope;

/**
 * 게시글 레포지토리
 */
@Repository
public interface InfoPostRepository extends JpaRepository<InfoPost, Long> {

    /**
     * 게시글 목록 조회 (최적화: attachments 제외, content는 미리보기용으로 포함)
     */
    @Query("SELECT new com.cs.info.controller.response.PostListResponse(" +
           "p.id, p.boardId, p.title, p.regTime, " +
           "p.writerId, p.writerName, p.isPrivate, p.viewCount, p.pinned, " +
           "p.createdBy, p.modifiedBy, p.content, p.visibilityScope) " +
           "FROM InfoPost p " +
           "WHERE p.enabled = true AND p.boardId = :boardId")
    Page<PostListResponse> findAllForListByBoardId(@Param("boardId") Long boardId, Pageable pageable);

    /**
     * 전체 게시글 목록 조회 (게시판 구분 없음)
     */
    @Query("SELECT new com.cs.info.controller.response.PostListResponse(" +
           "p.id, p.boardId, p.title, p.regTime, " +
           "p.writerId, p.writerName, p.isPrivate, p.viewCount, p.pinned, " +
           "p.createdBy, p.modifiedBy, p.content, p.visibilityScope) " +
           "FROM InfoPost p " +
           "WHERE p.enabled = true")
    Page<PostListResponse> findAllForList(Pageable pageable);

    /**
     * 게시글 목록 조회 - 가시성 필터 적용 (최적화)
     */
    @Query("SELECT new com.cs.info.controller.response.PostListResponse(" +
           "p.id, p.boardId, p.title, p.regTime, " +
           "p.writerId, p.writerName, p.isPrivate, p.viewCount, p.pinned, " +
           "p.createdBy, p.modifiedBy, p.content, p.visibilityScope) " +
           "FROM InfoPost p " +
           "WHERE p.enabled = true AND p.boardId = :boardId " +
           "AND (p.visibilityScope IN :scopes OR p.visibilityScope IS NULL)")
    Page<PostListResponse> findAllForListByBoardIdAndVisibilityScope(
            @Param("boardId") Long boardId,
            @Param("scopes") List<VisibilityScope> scopes,
            Pageable pageable);
}

