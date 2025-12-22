package com.cs.hrm.entity;

import com.cs.core.entity.AuditTimeEntity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "hrm_employment_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class HrmEmploymentType extends AuditTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employment_type_id")
    @Comment("고용형태 ID")
    private Integer employmentTypeId;

    @Column(name = "employment_type_name")
    @Comment("고용형태 Name")
    private String employmentTypeName;
}
