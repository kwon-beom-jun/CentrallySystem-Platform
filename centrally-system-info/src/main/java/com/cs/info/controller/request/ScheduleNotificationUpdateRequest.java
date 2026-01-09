package com.cs.info.controller.request;

import lombok.Getter;
import lombok.Setter;

/**
 * 일정 알림 설정 수정 요청
 */
@Getter
@Setter
public class ScheduleNotificationUpdateRequest {
    private String notificationTimings; // JSON 배열
    private String timezone;
    private String weekdayStartTime; // HH:mm
    private String weekdayEndTime; // HH:mm
    private Boolean weekendEnabled;
    private Boolean holidayEnabled;
    private String scheduleTypeNotifications; // JSON 객체
    private Boolean emailEnabled;
    private Boolean smsEnabled;
    private Boolean pushEnabled;
}

