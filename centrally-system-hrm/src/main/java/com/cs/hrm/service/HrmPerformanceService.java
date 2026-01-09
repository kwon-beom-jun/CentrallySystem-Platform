package com.cs.hrm.service;

import com.cs.hrm.entity.HrmPerformance;

import java.util.List;

/**
 * 성과 서비스 인터페이스
 */
public interface HrmPerformanceService {

    /**
     * 사용자 id를 이용해 성과 데이터를 조회합니다.
     * @param userId 사용자 ID
     * @return 성과 목록
     */
    List<HrmPerformance> getPerformancesByUserId(Integer userId);

    /**
     * 특정 사용자에서 특정 성과를 삭제합니다.
     * 사용자 id와 성과 id가 일치하는 경우에만 삭제합니다.
     * @param userId 사용자 id
     * @param performanceId 삭제할 성과의 id
     */
    void deletePerformance(Integer userId, Integer performanceId);

    /**
     * 특정 사용자에 새로운 성과 데이터를 추가합니다.
     * @param userId 사용자 id
     * @param performance 추가할 성과 데이터 (userId는 무시하고 경로의 userId로 설정)
     * @return 추가된 성과 데이터
     */
    HrmPerformance createPerformance(Integer userId, HrmPerformance performance);

    /**
     * 특정 사용자에서 특정 성과 데이터를 수정합니다.
     * @param userId 사용자 id
     * @param performanceId 성과 id
     * @param updatedPerformance 수정할 성과 데이터
     * @return 수정된 성과 데이터
     */
    HrmPerformance patchPerformance(Integer userId, Integer performanceId, HrmPerformance updatedPerformance);
}
