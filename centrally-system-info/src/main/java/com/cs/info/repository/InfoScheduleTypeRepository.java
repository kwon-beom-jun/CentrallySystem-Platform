package com.cs.info.repository;

import com.cs.info.entity.InfoScheduleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 일정 유형 Repository
 */
@Repository
public interface InfoScheduleTypeRepository extends JpaRepository<InfoScheduleType, Long> {

    /**
     * 활성화된 일정 유형 목록 조회 (표시 순서 기준)
     */
    List<InfoScheduleType> findByEnabledTrueAndIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * 삭제되지 않은 일정 유형 목록 조회 (표시 순서 기준, isActive 필터링 없음)
     */
    List<InfoScheduleType> findByEnabledTrueOrderByDisplayOrderAsc();

    /**
     * 코드로 일정 유형 조회
     */
    Optional<InfoScheduleType> findByCode(String code);

    /**
     * 코드로 활성화된 일정 유형 조회
     */
    Optional<InfoScheduleType> findByCodeAndEnabledTrueAndIsActiveTrue(String code);

    /**
     * 코드로 삭제되지 않은 일정 유형 조회 (enabled=true인 것만)
     */
    Optional<InfoScheduleType> findByCodeAndEnabledTrue(String code);

    /**
     * 활성화된 일정 유형 개수 조회
     */
    long countByEnabledTrueAndIsActiveTrue();
}

