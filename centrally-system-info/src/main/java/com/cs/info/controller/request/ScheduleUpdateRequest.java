package com.cs.info.controller.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 일정 수정 요청
 */
@Getter
@Setter
public class ScheduleUpdateRequest {
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer assigneeId;
    private String assigneeName;
    private String assigneeEmail;
    private Integer departmentId;
    private String departmentName;
    private String description;
    private String color;
    private String scheduleType; // 일정 유형 코드 (문자열)
    private String notificationTimings; // 일정별 알림 시점 설정 (JSON 배열 문자열)
}

