package com.cs.info.entity;

import com.cs.core.entity.SoftDeleteEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;

/**
 * 일정 알림 설정 엔티티
 */
@Entity
@Table(
    name = "info_schedule_notification",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id"})
    },
    indexes = {
        @Index(name = "idx_schedule_notification_user_id", columnList = "user_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE info_schedule_notification SET enabled = false, deleted_at = now() WHERE notification_id = ?")
public class InfoScheduleNotification extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    @Comment("알림 설정 ID")
    private Long notificationId;

    @Column(name = "user_id", nullable = false, unique = true)
    @Comment("사용자 ID")
    private Integer userId;

    /**
     * 알림 시점 설정 (JSON 배열)
     * 예: ["1일전", "3일전", "1시간전", "30분전"]
     */
    @Column(name = "notification_timings", columnDefinition = "TEXT")
    @Comment("알림 시점 설정 (JSON 배열)")
    private String notificationTimings;

    /**
     * 시간대 설정
     */
    @Column(name = "timezone", length = 50)
    @Comment("시간대")
    @Builder.Default
    private String timezone = "Asia/Seoul";

    @Column(name = "weekday_start_time", length = 5)
    @Comment("평일 시작 시간 (HH:mm)")
    @Builder.Default
    private String weekdayStartTime = "09:00";

    @Column(name = "weekday_end_time", length = 5)
    @Comment("평일 종료 시간 (HH:mm)")
    @Builder.Default
    private String weekdayEndTime = "18:00";

    @Column(name = "weekend_enabled")
    @Comment("주말 알림 여부")
    @Builder.Default
    private Boolean weekendEnabled = false;

    @Column(name = "holiday_enabled")
    @Comment("휴일 알림 여부")
    @Builder.Default
    private Boolean holidayEnabled = false;

    /**
     * 일정 유형별 알림 ON/OFF (JSON 객체)
     * 예: {"BUSINESS_TRIP": true, "VACATION": false, "MEETING": true}
     */
    @Column(name = "schedule_type_notifications", columnDefinition = "TEXT")
    @Comment("일정 유형별 알림 설정 (JSON 객체)")
    private String scheduleTypeNotifications;

    /**
     * 알림 방법 설정
     */
    @Column(name = "email_enabled")
    @Comment("이메일 알림 여부")
    @Builder.Default
    private Boolean emailEnabled = true;

    @Column(name = "sms_enabled")
    @Comment("SMS 알림 여부 (추후)")
    @Builder.Default
    private Boolean smsEnabled = false;

    @Column(name = "push_enabled")
    @Comment("푸시 알림 여부 (추후)")
    @Builder.Default
    private Boolean pushEnabled = false;
}

