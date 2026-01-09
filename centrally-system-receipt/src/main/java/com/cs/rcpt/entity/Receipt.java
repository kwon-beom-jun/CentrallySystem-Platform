package com.cs.rcpt.entity;

import java.sql.Date;
import java.math.BigDecimal;
import java.util.List;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.annotations.Comment;

import com.cs.core.entity.AuditTimeEntity;
import com.cs.core.entity.SoftDeleteEntity;
import com.cs.rcpt.enums.ReceiptProcessStatus;
import com.cs.rcpt.enums.ReceiptProcessStatusConverter;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.*;

/**
 *	추후 @Where -> @JdbcWhere(Hibernate 6.5 계열)
 */
@Entity
@Table(name = "receipt")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE receipt SET enabled = false, deleted_at = now() WHERE receipt_id = ?")
public class Receipt extends SoftDeleteEntity {

    /** 
     * receipt 테이블
     * PK: Receipt_ID (DDL상 AUTO_INCREMENT 없음 → 직접 할당)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receipt_id", nullable = false)
    @Comment("영수증 ID")
    private Integer receiptId;
    
    // YYMM-N  형태 (사용자, 관리자 식별 코드)
    @Column(name = "receipt_code", nullable = false, unique = true, length = 15)
    @Comment("식별 코드")
    private String receiptCode;

    @Column(name = "user_id")
    @Comment("사용자 ID")
    private Integer userId; // FK -> auth_users (다른 마이크로서비스)
    
    @Column(name = "user_name")
    @Comment("사용자 이름")
    private String userName;

    @Column(name = "user_email")
    @Comment("사용자 이메일")
    private String userEmail;

    @Column(name = "department_id")
    @Comment("부서 ID")
    private Integer departmentId;

    @Column(name = "department_name")
    @Comment("부서 이름")
    private String departmentName;

    @Column(name = "team_id")
    @Comment("팀 ID")
    private Integer teamId;

    @Column(name = "team_name")
    @Comment("팀 이름")
    private String teamName;

    @Column(name = "submission_date")
    @Comment("발생일")
    private Date submissionDate;

    @Column(name = "reason")
    @Comment("사유")
    private String reason;

    @Column(name = "amount")
    @Comment("금액")
    private BigDecimal amount;

    @Column(name = "last_approval_date")
    @Comment("마지막 승인 날짜")
    private Date lastApprovalDate;

    @Column(name = "last_rejection_date")
    @Comment("마지막 반려 날짜")
    private Date lastRejectionDate;

    @Column(name = "issuing_location")
    @Comment("발급위치")
    private String issuingLocation;
    
    /** 처음 INSERT 될 때 기본값 1 */
    // Lombok @Builder 로 생성할 때 기본값 유지
    @Builder.Default
    @Column(name = "current_approval_step")
    @Comment("현재 결재 단계")
    private Integer currentApprovalStep = 1;

    @Convert(converter = ReceiptProcessStatusConverter.class)
    @Column(name = "status_code", nullable = false)
    @Comment("영수증 상태 코드")
    private ReceiptProcessStatus status;

    /**
     * 1:1 (ReceiptAttachments)
     * receipt 테이블에 Attachment_ID FK
     * Receipt가 주인(Owner)
     */
    @OneToOne(cascade = CascadeType.ALL)
    @Where(clause = "enabled = true")
    @JoinColumn(name = "attachment_id", referencedColumnName = "attachment_id")
    @Comment("첨부 파일 ID")
    @JsonManagedReference("receipt_attachment")
    private ReceiptAttachments attachment;

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "category_id", nullable = false)
    @Comment("카테고리_ID")
    @JsonManagedReference("category_id")
    private ReceiptCategory category;

    /**
     * 1:N (ReceiptParticipants)
     * receipt_participants 테이블에 Receipt_ID FK
     */
    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
    @Where(clause = "enabled = true")
    @JsonManagedReference("receipt_participants")
    private List<ReceiptParticipants> participantsList;

    /**
     * 1:N (ReceiptApprovalLine)
     * receipt_approval_line 테이블에 Receipt_ID FK
     */
    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
    @Where(clause = "enabled = true")
    @JsonManagedReference("receipt_approval_line")
    @OrderBy("stepOrder ASC")          // 항상 1,2,3,… 순으로 정렬
    private List<ReceiptApprovalLine> approvalLines;
    
    
    /** JPA persist 직전에 한번 더 안전장치 */
    @PrePersist
    private void prePersist() {
        if (currentApprovalStep == null) {
            currentApprovalStep = 1;
        }
    }
}
