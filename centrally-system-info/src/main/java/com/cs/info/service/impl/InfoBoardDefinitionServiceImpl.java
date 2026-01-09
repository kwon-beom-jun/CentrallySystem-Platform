package com.cs.info.service.impl;

import com.cs.core.handler.GlobalExceptionHandler;
import com.cs.info.entity.InfoBoardDefinition;
import com.cs.info.enums.BoardCode;
import com.cs.info.repository.InfoBoardDefinitionRepository;
import com.cs.info.service.InfoBoardDefinitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 게시판 정의 서비스 구현체
 */
@Service
@Slf4j
public class InfoBoardDefinitionServiceImpl implements InfoBoardDefinitionService {

    @Autowired
    private InfoBoardDefinitionRepository infoBoardDefinitionRepository;

    /**
     * 게시판 코드로 조회
     */
    @Override
    @Transactional(readOnly = true)
    public InfoBoardDefinition getBoardByCode(BoardCode boardCode) {
        return infoBoardDefinitionRepository.findByBoardCodeAndEnabledTrue(boardCode)
                .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "게시판을 찾을 수 없습니다: " + boardCode));
    }

    /**
     * 모든 활성 게시판 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<InfoBoardDefinition> getAllActiveBoards() {
        return infoBoardDefinitionRepository.findAllByEnabledTrue();
    }

    /**
     * 게시판 생성
     */
    @Override
    @Transactional
    public InfoBoardDefinition createBoard(InfoBoardDefinition board) {
        return infoBoardDefinitionRepository.save(board);
    }

    /**
     * 게시판 수정
     */
    @Override
    @Transactional
    public InfoBoardDefinition updateBoard(Long id, InfoBoardDefinition boardDetails) {
        InfoBoardDefinition board = infoBoardDefinitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "게시판을 찾을 수 없습니다: " + id));
        
        board.setName(boardDetails.getName());
        board.setDescription(boardDetails.getDescription());
        board.setIsActive(boardDetails.getIsActive());
        
        return infoBoardDefinitionRepository.save(board);
    }

    /**
     * 게시판 삭제 (소프트 딜리트)
     */
    @Override
    @Transactional
    public void deleteBoard(Long id) {
        InfoBoardDefinition board = infoBoardDefinitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "게시판을 찾을 수 없습니다: " + id));
        
        infoBoardDefinitionRepository.delete(board);
    }
}

