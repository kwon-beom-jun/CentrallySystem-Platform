package com.cs.rcpt.entity;

import com.cs.core.entity.AuditTimeEntity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "receipt_closings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptClosings extends AuditTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "closing_id")
    @Comment("마감 ID")
    private Integer closingId;

    @Column(name = "user_id")
    @Comment("사용자 ID")
    private Integer userId; // FK -> auth_users

    @Column(name = "closing_date")
    @Comment("마감 날짜")
    private java.sql.Date closingDate;

    @Column(name = "total_amount")
    @Comment("총 금액")
    private Integer totalAmount;

    @Column(name = "remarks")
    @Comment("비고")
    private String remarks;
}
