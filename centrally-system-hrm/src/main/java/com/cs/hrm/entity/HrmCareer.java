package com.cs.hrm.entity;

import com.cs.core.entity.AuditTimeEntity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "hrm_career")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class HrmCareer extends AuditTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "career_id")
    @Comment("경력 ID")
    private Integer careerId;

    @Column(name = "user_id")
    @Comment("사용자 ID")
    private Integer userId; // FK -> auth_users

    @Column(name = "company")
    @Comment("회사")
    private String company;

    @Column(name = "position")
    @Comment("직책")
    private String position;

    @Column(name = "joining_date")
    @Comment("입사일")
    private java.sql.Date joiningDate;

    @Column(name = "leaving_date")
    @Comment("퇴사일")
    private java.sql.Date leavingDate;
}
