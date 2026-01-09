package com.cs.hrm.service;

import com.cs.hrm.entity.HrmStyle;

import java.util.List;
import java.util.Map;

/**
 * 스타일 서비스 인터페이스
 */
public interface HrmStyleService {

    /**
     * 모든 스타일 조회
     * @return 스타일 목록
     */
    List<HrmStyle> getAllStyles();

    /**
     * 카테고리별 스타일 조회
     * @param category 스타일 카테고리
     * @return 스타일 목록
     */
    List<HrmStyle> getStylesByCategory(String category);

    /**
     * 사용자의 모든 스타일 설정 조회 (Map 형태)
     * @param userId 사용자 ID
     * @return 카테고리별 스타일 코드 Map
     */
    Map<String, String> getUserStyleCodes(Integer userId);

    /**
     * 사용자 스타일 업데이트 (카테고리별)
     * @param userId 사용자 ID
     * @param category 스타일 카테고리
     * @param styleCode 스타일 코드
     */
    void updateUserStyle(Integer userId, String category, String styleCode);

    /**
     * 메인 카드 스타일 업데이트 (기존 API 호환용)
     * @param userId 사용자 ID
     * @param styleCode 스타일 코드
     */
    void updateMainCardStyle(Integer userId, String styleCode);

    /**
     * 기본 정보 모바일 스타일 업데이트
     * @param userId 사용자 ID
     * @param styleCode 스타일 코드
     */
    void updateInfoMobileStyle(Integer userId, String styleCode);
}
