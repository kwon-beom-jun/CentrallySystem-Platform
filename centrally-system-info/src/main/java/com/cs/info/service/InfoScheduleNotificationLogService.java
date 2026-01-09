package com.cs.info.service;

import com.cs.core.vo.page.PageResponseVo;
import com.cs.info.controller.response.ScheduleNotificationLogResponse;

import java.time.LocalDate;

/**
 * 일정 알림 발송 이력 서비스 인터페이스
 */
public interface InfoScheduleNotificationLogService {

    /**
     * 알림 발송 이력 페이징 조회
     * @param startDate 시작 날짜 (선택)
     * @param endDate 종료 날짜 (선택)
     * @param searchKeyword 검색어 (사용자명, 이메일)
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 페이징된 알림 발송 이력
     */
    PageResponseVo<ScheduleNotificationLogResponse> getNotificationHistory(
            LocalDate startDate,
            LocalDate endDate,
            String searchKeyword,
            int page,
            int size);
}
