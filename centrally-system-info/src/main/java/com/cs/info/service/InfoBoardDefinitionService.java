package com.cs.info.service;

import com.cs.info.entity.InfoBoardDefinition;
import com.cs.info.enums.BoardCode;

import java.util.List;

/**
 * 게시판 정의 서비스 인터페이스
 */
public interface InfoBoardDefinitionService {

    /**
     * 게시판 코드로 조회
     * @param boardCode 게시판 코드
     * @return 게시판 정의 엔티티
     */
    InfoBoardDefinition getBoardByCode(BoardCode boardCode);

    /**
     * 모든 활성 게시판 조회
     * @return 활성 게시판 목록
     */
    List<InfoBoardDefinition> getAllActiveBoards();

    /**
     * 게시판 생성
     * @param board 게시판 정의 엔티티
     * @return 생성된 게시판 정의 엔티티
     */
    InfoBoardDefinition createBoard(InfoBoardDefinition board);

    /**
     * 게시판 수정
     * @param id 게시판 ID
     * @param boardDetails 수정할 게시판 정보
     * @return 수정된 게시판 정의 엔티티
     */
    InfoBoardDefinition updateBoard(Long id, InfoBoardDefinition boardDetails);

    /**
     * 게시판 삭제 (소프트 딜리트)
     * @param id 게시판 ID
     */
    void deleteBoard(Long id);
}
