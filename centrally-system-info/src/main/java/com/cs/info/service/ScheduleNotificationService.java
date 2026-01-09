package com.cs.info.service;

import java.time.LocalDateTime;

/**
 * 일정 알림 발송 서비스 인터페이스
 */
public interface ScheduleNotificationService {

    /**
     * 알림 발송 대상 정보 DTO
     */
    class NotificationTarget {
        private Long scheduleId;
        private Integer userId;
        private String notificationType; // "1일전", "1시간전" 등
        private LocalDateTime scheduledNotificationTime; // 예정 알림 시간
        private LocalDateTime originalStartTime; // 일정 생성/수정 시점의 시작 시간 (변경 감지용)
        private LocalDateTime createdAt; // 알림 대상이 생성된 시점

        public NotificationTarget(Long scheduleId, Integer userId, String notificationType,
                                LocalDateTime scheduledNotificationTime, LocalDateTime originalStartTime) {
            this.scheduleId = scheduleId;
            this.userId = userId;
            this.notificationType = notificationType;
            this.scheduledNotificationTime = scheduledNotificationTime;
            this.originalStartTime = originalStartTime;
            this.createdAt = LocalDateTime.now();
        }

        // Getters
        public Long getScheduleId() { return scheduleId; }
        public Integer getUserId() { return userId; }
        public String getNotificationType() { return notificationType; }
        public LocalDateTime getScheduledNotificationTime() { return scheduledNotificationTime; }
        public LocalDateTime getOriginalStartTime() { return originalStartTime; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }

    /**
     * 알림 발송 (발송 직전 검증 포함) - 비동기
     * @param target 알림 발송 대상
     */
    void sendNotificationAsync(NotificationTarget target);

    /**
     * 알림 발송 (발송 직전 검증 포함)
     * @param target 알림 발송 대상
     */
    void sendNotification(NotificationTarget target);
}
