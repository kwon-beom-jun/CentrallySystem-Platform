package com.cs.info.entity;

import com.cs.core.entity.SoftDeleteEntity;
import com.fasterxml.jackson.annotation.JsonGetter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 일정 엔티티
 */
@Entity
@Table(name = "info_schedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE info_schedule SET enabled = false, deleted_at = now() WHERE schedule_id = ?")
public class InfoSchedule extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    @Comment("일정 ID")
    private Long scheduleId;

    @Column(name = "title", nullable = false)
    @Comment("제목")
    private String title;

    @Column(name = "start_date", nullable = false)
    @Comment("시작일")
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    @Comment("종료일")
    private LocalDate endDate;

    @Column(name = "start_time")
    @Comment("시작 시간")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    @Comment("종료 시간")
    private LocalDateTime endTime;

    @Column(name = "assignee_id", nullable = false)
    @Comment("담당자 ID")
    private Integer assigneeId;

    @Column(name = "assignee_name")
    @Comment("담당자명")
    private String assigneeName;

    @Column(name = "assignee_email")
    @Comment("담당자 이메일")
    private String assigneeEmail;

    @Column(name = "department_id")
    @Comment("부서 ID")
    private Integer departmentId;

    @Column(name = "department_name")
    @Comment("부서명")
    private String departmentName;

    @Column(name = "description", columnDefinition = "TEXT")
    @Comment("설명")
    private String description;

    @Column(name = "color")
    @Comment("색상 (HEX 코드)")
    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_type_id", nullable = false)
    @Comment("일정 유형")
    private InfoScheduleType scheduleType;

    /**
     * JSON 직렬화 시 scheduleType 코드 반환
     */
    @JsonGetter("scheduleType")
    public String getScheduleTypeCode() {
        if (scheduleType != null) {
            return scheduleType.getCode();
        }
        return null;
    }

    @Column(name = "creator_id", nullable = false)
    @Comment("작성자 ID")
    private Integer creatorId;

    @Column(name = "creator_name")
    @Comment("작성자명")
    private String creatorName;

    /**
     * 일정별 알림 시점 설정 (JSON 배열)
     * 예: ["1일전", "1시간전", "30분전"]
     * null이면 전역 알림 설정 사용
     */
    @Column(name = "notification_timings", columnDefinition = "TEXT")
    @Comment("일정별 알림 시점 설정 (JSON 배열)")
    private String notificationTimings;
}

