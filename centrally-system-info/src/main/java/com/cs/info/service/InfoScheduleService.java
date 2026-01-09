package com.cs.info.service;

import com.cs.info.controller.request.ScheduleCreateRequest;
import com.cs.info.controller.request.ScheduleUpdateRequest;
import com.cs.info.entity.InfoSchedule;

import java.time.LocalDate;
import java.util.List;

/**
 * 일정 서비스 인터페이스
 */
public interface InfoScheduleService {

    /**
     * 일정 목록 조회 (기간별)
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param departmentId 부서 ID (선택)
     * @param assigneeId 담당자 ID (선택)
     * @return 일정 목록
     */
    List<InfoSchedule> getSchedules(LocalDate startDate, LocalDate endDate, Integer departmentId, Integer assigneeId);

    /**
     * 일정 단건 조회
     * @param scheduleId 일정 ID
     * @return 일정 엔티티
     */
    InfoSchedule getSchedule(Long scheduleId);

    /**
     * 일정 생성
     * @param request 일정 생성 요청
     * @param creatorId 생성자 ID
     * @param creatorName 생성자 이름
     * @return 생성된 일정 엔티티
     */
    InfoSchedule createSchedule(ScheduleCreateRequest request, Integer creatorId, String creatorName);

    /**
     * 일정 수정 (권한 검증 필요)
     * @param scheduleId 일정 ID
     * @param request 일정 수정 요청
     * @param editorUserId 수정자 ID
     * @param isManager 관리자 여부
     * @return 수정된 일정 엔티티
     */
    InfoSchedule updateSchedule(Long scheduleId, ScheduleUpdateRequest request, Integer editorUserId, boolean isManager);

    /**
     * 일정 삭제 (소프트 딜리트, 권한 검증 필요)
     * @param scheduleId 일정 ID
     * @param userId 사용자 ID
     * @param isManager 관리자 여부
     */
    void deleteSchedule(Long scheduleId, Integer userId, boolean isManager);
}
