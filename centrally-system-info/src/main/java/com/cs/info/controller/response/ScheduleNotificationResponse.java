package com.cs.info.controller.response;

import com.cs.info.entity.InfoScheduleNotification;
import lombok.Builder;
import lombok.Getter;

/**
 * 일정 알림 설정 응답
 */
@Getter
@Builder
public class ScheduleNotificationResponse {
    private final Long notificationId;
    private final Integer userId;
    private final String notificationTimings; // JSON 배열
    private final String timezone;
    private final String weekdayStartTime;
    private final String weekdayEndTime;
    private final Boolean weekendEnabled;
    private final Boolean holidayEnabled;
    private final String scheduleTypeNotifications; // JSON 객체
    private final Boolean emailEnabled;
    private final Boolean smsEnabled;
    private final Boolean pushEnabled;

    /**
     * InfoScheduleNotification 엔티티를 ScheduleNotificationResponse DTO로 변환하는 정적 팩토리 메서드
     */
    public static ScheduleNotificationResponse from(InfoScheduleNotification notification) {
        return ScheduleNotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .userId(notification.getUserId())
                .notificationTimings(notification.getNotificationTimings())
                .timezone(notification.getTimezone())
                .weekdayStartTime(notification.getWeekdayStartTime())
                .weekdayEndTime(notification.getWeekdayEndTime())
                .weekendEnabled(notification.getWeekendEnabled())
                .holidayEnabled(notification.getHolidayEnabled())
                .scheduleTypeNotifications(notification.getScheduleTypeNotifications())
                .emailEnabled(notification.getEmailEnabled())
                .smsEnabled(notification.getSmsEnabled())
                .pushEnabled(notification.getPushEnabled())
                .build();
    }
}

