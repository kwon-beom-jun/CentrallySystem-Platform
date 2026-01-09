package com.cs.info.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cs.core.vo.page.PageResponseVo;
import com.cs.info.controller.response.ScheduleNotificationLogResponse;
import com.cs.info.service.InfoScheduleNotificationLogService;

/**
 * 일정 알림 발송 이력 컨트롤러
 */
@RestController
@RequestMapping("/schedule-notification-logs")
public class InfoScheduleNotificationLogController {

    @Autowired
    private InfoScheduleNotificationLogService logService;

    /**
     * 알림 발송 이력 페이징 조회
     * @param startDate 시작 날짜 (선택, 기본값: 오늘)
     * @param endDate 종료 날짜 (선택, 기본값: 오늘)
     * @param searchKeyword 검색어 (사용자명, 이메일)
     * @param page 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return 페이징된 알림 발송 이력
     */
    @GetMapping
    public ResponseEntity<PageResponseVo<ScheduleNotificationLogResponse>> getNotificationHistory(
            @RequestParam(name = "startDate", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        
        // 시작일/종료일이 없으면 오늘 날짜로 설정
        LocalDate today = LocalDate.now();
        LocalDate finalStartDate = startDate != null ? startDate : today;
        LocalDate finalEndDate = endDate != null ? endDate : today;
        
        PageResponseVo<ScheduleNotificationLogResponse> result = logService.getNotificationHistory(
                finalStartDate,
                finalEndDate,
                searchKeyword,
                page,
                size
        );
        
        return ResponseEntity.ok(result);
    }
}

