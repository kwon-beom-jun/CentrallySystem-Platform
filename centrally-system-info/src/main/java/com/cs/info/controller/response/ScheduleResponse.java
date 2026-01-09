package com.cs.info.controller.response;

import com.cs.info.entity.InfoSchedule;
import com.cs.info.entity.InfoScheduleType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 일정 응답
 */
@Getter
@Builder
public class ScheduleResponse {
    private final Long scheduleId;
    private final String title;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final Integer assigneeId;
    private final String assigneeName;
    private final String assigneeEmail;
    private final Integer departmentId;
    private final String departmentName;
    private final String description;
    private final String color;
    private final ScheduleTypeInfo scheduleTypeInfo; // 일정 유형 정보 (삭제된 일정 유형 포함)
    private final String notificationTimings; // 일정별 알림 시점 설정 (JSON 배열 문자열)
    private final Integer creatorId;
    private final String creatorName;
    private final LocalDateTime regTime; // 등록일시

    /**
     * 일정 유형 정보 (삭제된 일정 유형도 포함)
     */
    @Getter
    @Builder
    public static class ScheduleTypeInfo {
        private final String code;
        private final String label;
        private final String color;
    }

    /**
     * InfoSchedule 엔티티를 ScheduleResponse DTO로 변환하는 정적 팩토리 메서드
     */
    public static ScheduleResponse from(InfoSchedule schedule) {
        ScheduleTypeInfo scheduleTypeInfo = null;
        if (schedule.getScheduleType() != null) {
            InfoScheduleType type = schedule.getScheduleType();
            scheduleTypeInfo = ScheduleTypeInfo.builder()
                    .code(type.getCode())
                    .label(type.getLabel())
                    .color(type.getColor())
                    .build();
        }
        
        return ScheduleResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .title(schedule.getTitle())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .assigneeId(schedule.getAssigneeId())
                .assigneeName(schedule.getAssigneeName())
                .assigneeEmail(schedule.getAssigneeEmail())
                .departmentId(schedule.getDepartmentId())
                .departmentName(schedule.getDepartmentName())
                .description(schedule.getDescription())
                .color(schedule.getColor())
                .scheduleTypeInfo(scheduleTypeInfo)
                .notificationTimings(schedule.getNotificationTimings())
                .creatorId(schedule.getCreatorId())
                .creatorName(schedule.getCreatorName())
                .regTime(schedule.getRegTime())
                .build();
    }
}

