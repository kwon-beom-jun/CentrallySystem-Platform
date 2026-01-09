package com.cs.info.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cs.info.entity.InfoBoardDefinition;
import com.cs.info.enums.BoardCode;
import com.cs.info.service.InfoBoardDefinitionService;

/**
 * 게시판 정의 컨트롤러
 */
@RestController
@RequestMapping("/boards")
public class InfoBoardDefinitionController {

    @Autowired
    private InfoBoardDefinitionService infoBoardDefinitionService;

    /**
     * 게시판 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<InfoBoardDefinition>> getAllBoards() {
        List<InfoBoardDefinition> boards = infoBoardDefinitionService.getAllActiveBoards();
        return ResponseEntity.ok(boards);
    }

    /**
     * 게시판 단건 조회 (코드로)
     */
    @GetMapping("/{boardCode}")
    public ResponseEntity<InfoBoardDefinition> getBoardByCode(@PathVariable("boardCode") BoardCode boardCode) {
        InfoBoardDefinition board = infoBoardDefinitionService.getBoardByCode(boardCode);
        return ResponseEntity.ok(board);
    }

    /**
     * 게시판 생성 (ADMIN)
     */
    @PostMapping
    public ResponseEntity<InfoBoardDefinition> createBoard(@RequestBody InfoBoardDefinition board) {
        InfoBoardDefinition saved = infoBoardDefinitionService.createBoard(board);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * 게시판 수정 (ADMIN)
     */
    @PatchMapping("/{id}")
    public ResponseEntity<InfoBoardDefinition> updateBoard(
            @PathVariable("id") Long id,
            @RequestBody InfoBoardDefinition board) {
        InfoBoardDefinition updated = infoBoardDefinitionService.updateBoard(id, board);
        return ResponseEntity.ok(updated);
    }

    /**
     * 게시판 삭제 (ADMIN)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable("id") Long id) {
        infoBoardDefinitionService.deleteBoard(id);
        return ResponseEntity.noContent().build();
    }
}

