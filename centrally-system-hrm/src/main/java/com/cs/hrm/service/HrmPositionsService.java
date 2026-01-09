package com.cs.hrm.service;

import com.cs.hrm.entity.HrmPositions;

import java.util.List;
import java.util.Optional;

/**
 * 직책 서비스 인터페이스
 */
public interface HrmPositionsService {

    /**
     * 전체 직책 정보 조회
     * @return 직책 목록
     */
    List<HrmPositions> getPositions();

    /**
     * ID로 직책 조회
     * @param positionId 직책 ID
     * @return 직책 엔티티
     */
    HrmPositions getPosition(Integer positionId);

    /**
     * 포지션 ID로 특정 직책 조회
     * @param positionId 직책 ID
     * @return 직책 Optional
     */
    Optional<HrmPositions> getPositionById(Integer positionId);

    /**
     * 직책 삭제
     * @param positionId 직책 ID
     */
    void deletePosition(Integer positionId);
}
