package com.cs.info.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.cs.info.entity.InfoBoardDefinition;
import com.cs.info.enums.BoardCode;
import com.cs.info.repository.InfoBoardDefinitionRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * 게시판 초기 데이터 생성
 */
@Configuration
@Slf4j
public class DataInitializerBoards {

    @Autowired
    private InfoBoardDefinitionRepository infoBoardDefinitionRepository;

    /**
     * 게시판 Seed 데이터
     */
    private static final List<BoardSeed> BOARD_SEEDS = List.of(
        new BoardSeed(
            BoardCode.NOTICE,
            "공지사항",
            "시스템 공지사항 게시판입니다."
        ),
        new BoardSeed(
            BoardCode.RESOURCE,
            "자료실",
            "업무 관련 자료를 공유하는 게시판입니다."
        ),
        new BoardSeed(
            BoardCode.COMMUNITY,
            "자유게시판",
            "자유롭게 소통하는 게시판입니다."
        )
    );

    /**
     * 포트폴리오용: 데이터 초기화 기능 비활성화
     */

    /**
     * 게시판 Seed 레코드
     */
    private static record BoardSeed(
        BoardCode boardCode,
        String name,
        String description
    ) {}
}

