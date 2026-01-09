package com.cs.rcpt.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Comment;

import com.cs.core.entity.SoftDeleteEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "receipt_approval_line")
@SQLDelete(sql = "UPDATE receipt_approval_line SET enabled = false, deleted_at = now() WHERE approval_line_id = ?")
public class ReceiptApprovalLine extends SoftDeleteEntity {

    /** 결재선 PK */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approval_line_id", nullable = false)
    @Comment("결재_ID")
    private Integer approvalLineId;

    /** 결재 단계(순서) */
    @Column(name = "step_order")
    @Comment("결재 단계(순서)")
    private Integer stepOrder;

    /** 결재 역할(1-결재, 2-합의, 3-기본 합의자 등) */
    @Column(name = "approval_role")
    @Comment("결재 역할(유형)")
    private Integer approvalRole;

    /** 실제 결재자(사용자) ID */
    @Column(name = "approver_user_id")
    @Comment("결재자 사용자 ID")
    private Integer approverUserId;

    /** 실제 결재자 이름 */
    @Column(name = "approver_name")
    @Comment("결재자 이름")
    private String approverName;

    /** 실제 결재자 이메일 */
    @Column(name = "approver_email")
    @Comment("결재자 이메일")
    private String approverEmail;

    /** 승인 일자 */
    @Column(name = "approved_at")
    @Comment("승인 일시")
    private LocalDateTime approvedAt;

    /** 반려 일자 */
    @Column(name = "rejected_at")
    @Comment("반려 일시")
    private LocalDateTime rejectedAt;

    /** 반려 사유 */
    @Column(name = "rejected_reason")
    @Comment("반려 사유")
    private String rejectedReason;

    /** 승인 상태(true=승인, false=대기/반려) */
    @Column(name = "approval_status")
    @Comment("승인 상태")
    private Boolean approvalStatus;

    @Column(name = "department")
    @Comment("결재자 부서")
    private String department;

    @Column(name = "team")
    @Comment("결재자 팀")
    private String team;

    /** 대리결재 매핑 (대리자가 승인/반려한 경우 연결) */
    @ManyToOne
    @JoinColumn(name = "delegate_id", referencedColumnName = "delegate_id")
    @Comment("대리결재 매핑 ID")
    private ReceiptApproverDelegate delegateMapping;

    /**
     * N:1 (Receipt)
     * receipt_participants 테이블에 Receipt_ID 존재 (FK)
     */
    @ManyToOne
    @JoinColumn(name = "receipt_id", referencedColumnName = "receipt_id", nullable = false)
    @Comment("영수증 ID")
    @JsonBackReference("receipt_approvers")
    private Receipt receipt;
}
