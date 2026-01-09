package com.cs.info.entity;

import com.cs.core.entity.AuditTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 일정 알림 발송 이력 엔티티
 */
@Entity
@Table(
    name = "info_schedule_notification_log",
    indexes = {
        @Index(name = "idx_notification_log_schedule_id", columnList = "schedule_id"),
        @Index(name = "idx_notification_log_user_id", columnList = "user_id"),
        @Index(name = "idx_notification_log_sent", columnList = "sent"),
        @Index(name = "idx_notification_log_schedule_user_type", 
               columnList = "schedule_id,user_id,notification_type")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class InfoScheduleNotificationLog extends AuditTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    @Comment("로그 ID")
    private Long logId;

    @Column(name = "schedule_id", nullable = false)
    @Comment("일정 ID")
    private Long scheduleId;

    @Column(name = "user_id", nullable = false)
    @Comment("사용자 ID")
    private Integer userId;

    @Column(name = "user_name")
    @Comment("사용자명")
    private String userName;

    @Column(name = "user_email")
    @Comment("사용자 이메일")
    private String userEmail;

    @Column(name = "notification_type", length = 50, nullable = false)
    @Comment("알림 유형 (예: 1일전, 1시간전)")
    private String notificationType;

    @Column(name = "scheduled_time", nullable = false)
    @Comment("예정 알림 시간")
    private LocalDateTime scheduledTime;

    @Column(name = "sent_time")
    @Comment("실제 발송 시간")
    private LocalDateTime sentTime;

    @Column(name = "channel", length = 20, nullable = false)
    @Comment("발송 채널 (EMAIL, SMS, PUSH)")
    private String channel;

    @Column(name = "sent", nullable = false)
    @Comment("발송 성공 여부")
    @Builder.Default
    private Boolean sent = false;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    @Comment("실패 사유")
    private String failureReason;
}

