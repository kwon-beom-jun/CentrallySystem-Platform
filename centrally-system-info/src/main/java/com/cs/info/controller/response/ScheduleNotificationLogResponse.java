package com.cs.info.controller.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 일정 알림 발송 이력 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleNotificationLogResponse {
    private Long logId;
    private Long scheduleId;
    private Integer userId;
    private String userName;
    private String userEmail;
    
    /**
     * 알림 유형 (예: "[DAY] 일자", "[HOUR] 시간", "[MINUTE] 분")
     */
    private String notificationType;
    
    /**
     * 알림 데이터값 (예: "2025-12-19 16:13", "1시간 1분 전", "59분 전")
     */
    private String notificationValue;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:00")
    private LocalDateTime scheduledTime;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:00")
    private LocalDateTime sentTime;
    
    /**
     * 일정 시작 시간
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:00")
    private LocalDateTime scheduleStartTime;
    
    /**
     * 일정 종료 시간
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:00")
    private LocalDateTime scheduleEndTime;
    
    private String channel;
    private Boolean sent;
    private String failureReason;
}

