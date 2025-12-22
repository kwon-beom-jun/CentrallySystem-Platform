package com.cs.hrm.entity;

import com.cs.core.entity.AuditTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "hrm_performance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class HrmPerformance extends AuditTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "performance_id")
    @Comment("실적 ID")
    private Integer performanceId;

    @Column(name = "user_id")
    @Comment("사용자 ID")
    private Integer userId; // FK -> auth_users

    @Column(name = "from_date")
    @Comment("시작일")
    private LocalDate fromDate;

    @Column(name = "to_date")
    @Comment("종료일")
    private LocalDate toDate;

    @Column(name = "work_type")
    @Comment("유형")
    private String workType;

    @Column(name = "performance_title")
    @Comment("성과주제")
    private String performanceTitle;

    @Column(name = "performance")
    @Comment("성과")
    private String performance;

    @Column(name = "status")
    @Comment("상태")
    private Integer status;
}
