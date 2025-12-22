package com.cs.hrm.entity;

import com.cs.core.entity.AuditTimeEntity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "hrm_positions")
public class HrmPositions extends AuditTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "position_id")
    @Comment("직책 ID")
    private Integer positionId;

    @Column(name = "position_name")
    @Comment("직책 이름")
    private String positionName;
}
