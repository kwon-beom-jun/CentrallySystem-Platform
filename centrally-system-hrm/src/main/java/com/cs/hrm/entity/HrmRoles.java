package com.cs.hrm.entity;

import com.cs.core.entity.AuditTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

/**
 * Auth “권한 스냅샷” 전용 테이블
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "hrm_roles")
public class HrmRoles extends AuditTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")          // AUTH 역할 ID 그대로 사용
    @Comment("권한 ID")
    private Integer roleId;            // PK를 공유하면 업데이트가 편함

    @Column(name = "role_name", nullable = false)
    @Comment("권한 이름")
    private String roleName;           // ROLE_RECEIPT_APPROVER …

    @Column(name = "role_name_detail")
    @Comment("권한 이름 상세")
    private String roleNameDetail;     // ‘결재자’ …

    @Column(name = "service_name")
    @Comment("서비스 이름")
    private String serviceName;        // receipt, hrm, …
}
