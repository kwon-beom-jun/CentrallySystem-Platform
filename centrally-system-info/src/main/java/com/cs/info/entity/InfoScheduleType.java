package com.cs.info.entity;

import com.cs.core.entity.SoftDeleteEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;

/**
 * 일정 유형 엔티티
 */
@Entity
@Table(name = "info_schedule_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE info_schedule_type SET enabled = false, deleted_at = now(), display_order = -1 WHERE schedule_type_id = ?")
public class InfoScheduleType extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_type_id")
    @Comment("일정 유형 ID")
    private Long scheduleTypeId;

    @Column(name = "code", nullable = false)
    @Comment("일정 유형 코드")
    private String code;

    @Column(name = "label", nullable = false)
    @Comment("일정 유형 라벨")
    private String label;

    @Column(name = "color")
    @Comment("기본 색상 (HEX 코드)")
    private String color;

    @Column(name = "display_order")
    @Comment("표시 순서")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_active")
    @Comment("활성 여부")
    @Builder.Default
    private Boolean isActive = true;
}

