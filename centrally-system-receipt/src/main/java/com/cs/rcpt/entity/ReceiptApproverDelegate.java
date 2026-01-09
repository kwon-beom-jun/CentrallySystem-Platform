package com.cs.rcpt.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;

import com.cs.core.entity.SoftDeleteEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "receipt_approver_delegate")
@SQLDelete(sql = "UPDATE receipt_approver_delegate SET enabled = false, deleted_at = now() WHERE delegate_id = ?")
public class ReceiptApproverDelegate extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delegate_id")
    @Comment("대리결재 매핑 PK")
    private Long id;

    @Column(name = "principal_user_id", nullable = false)
    @Comment("원 결재자 사용자 ID")
    private Integer principalUserId;

    @Column(name = "delegate_user_id", nullable = false)
    @Comment("대리결재자 사용자 ID")
    private Integer delegateUserId;

    @Column(name = "effective_from")
    @Comment("유효 시작일시")
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    @Comment("유효 종료일시")
    private LocalDateTime effectiveTo;

    @Column(name = "delegate_name")
    @Comment("대리결재자 이름")
    private String delegateName;

    @Column(name = "delegate_email")
    @Comment("대리결재자 이메일")
    private String delegateEmail;

    @Column(name = "delegate_department")
    @Comment("대리결재자 부서")
    private String delegateDepartment;

    @Column(name = "delegate_team")
    @Comment("대리결재자 팀")
    private String delegateTeam;
}



