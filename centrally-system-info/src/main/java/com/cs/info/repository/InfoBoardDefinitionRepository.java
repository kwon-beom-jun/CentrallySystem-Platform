package com.cs.info.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cs.info.entity.InfoBoardDefinition;
import com.cs.info.enums.BoardCode;

/**
 * 게시판 정의 레포지토리
 */
@Repository
public interface InfoBoardDefinitionRepository extends JpaRepository<InfoBoardDefinition, Long> {

    /**
     * 게시판 코드로 활성 게시판 조회
     */
    Optional<InfoBoardDefinition> findByBoardCodeAndEnabledTrue(BoardCode boardCode);

    /**
     * 모든 활성 게시판 조회
     */
    List<InfoBoardDefinition> findAllByEnabledTrue();
}

