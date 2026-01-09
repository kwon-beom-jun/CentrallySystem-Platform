package com.cs.info.service;

import com.cs.info.entity.InfoScheduleType;

import java.util.List;

/**
 * 일정 유형 서비스 인터페이스
 */
public interface InfoScheduleTypeService {

    /**
     * 활성화된 일정 유형 목록 조회 (캘린더 등에서 사용)
     * @return 활성화된 일정 유형 목록
     */
    List<InfoScheduleType> getAllActiveScheduleTypes();

    /**
     * 삭제되지 않은 일정 유형 목록 조회 (관리 페이지에서 사용, isActive 필터링 없음)
     * @return 일정 유형 목록
     */
    List<InfoScheduleType> getAllScheduleTypes();

    /**
     * 캘린더용 일정 유형 목록 조회 (enabled=true이고 isActive=true인 항목만)
     * @return 활성화된 일정 유형 목록
     */
    List<InfoScheduleType> getAllScheduleTypesForCalendar();

    /**
     * 코드로 일정 유형 조회
     * @param code 일정 유형 코드
     * @return 일정 유형 엔티티
     */
    InfoScheduleType getScheduleTypeByCode(String code);

    /**
     * 일정 유형 단건 조회
     * @param scheduleTypeId 일정 유형 ID
     * @return 일정 유형 엔티티
     */
    InfoScheduleType getScheduleType(Long scheduleTypeId);

    /**
     * 일정 유형 생성
     * @param scheduleType 일정 유형 엔티티
     * @return 생성된 일정 유형 엔티티
     */
    InfoScheduleType createScheduleType(InfoScheduleType scheduleType);

    /**
     * 일정 유형 수정
     * @param scheduleTypeId 일정 유형 ID
     * @param scheduleTypeDetails 수정할 일정 유형 정보
     * @return 수정된 일정 유형 엔티티
     */
    InfoScheduleType updateScheduleType(Long scheduleTypeId, InfoScheduleType scheduleTypeDetails);

    /**
     * 일정 유형 삭제 (소프트 딜리트)
     * @param scheduleTypeId 일정 유형 ID
     */
    void deleteScheduleType(Long scheduleTypeId);
}
