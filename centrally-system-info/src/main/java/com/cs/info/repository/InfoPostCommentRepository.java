package com.cs.info.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cs.info.entity.InfoPost;
import com.cs.info.entity.InfoPostComment;

/**
 * 댓글 레포지토리
 */
@Repository
public interface InfoPostCommentRepository extends JpaRepository<InfoPostComment, Long> {

    /**
     * 게시글의 댓글 목록 조회 (시간순 정렬)
     */
    List<InfoPostComment> findByPostOrderByRegTimeAsc(InfoPost post);

    /**
     * 부모 댓글의 자식 댓글 조회
     */
    List<InfoPostComment> findByParent(InfoPostComment parent);
}

