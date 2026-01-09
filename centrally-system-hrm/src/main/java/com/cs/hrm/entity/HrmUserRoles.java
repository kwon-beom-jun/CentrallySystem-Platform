package com.cs.hrm.entity;

import com.cs.core.entity.AuditTimeEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

/**
 * Auth “권한 스냅샷” 전용 테이블
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "hrm_user_roles",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_hrm_user_role",
           columnNames = {"user_id","role_id"}))
public class HrmUserRoles extends AuditTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ---------- FK : 사용자 ---------- */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Comment("사용자 ID")
    @JsonBackReference("user_userRole_hrm")
    private HrmUser user;

    /* ---------- FK : 역할 ---------- */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    @Comment("권한 ID")
    @JsonBackReference("role_userRole_hrm")
    private HrmRoles role;
}
