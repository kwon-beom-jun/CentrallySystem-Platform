package com.cs.rcpt.entity;

import org.hibernate.annotations.SQLDelete;

import com.cs.core.entity.SoftDeleteEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "receipt_participants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE receipt_participants SET enabled = false, deleted_at = now() WHERE participation_id = ?")
public class ReceiptParticipants extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participation_id", nullable = false)
    @Comment("참여_ID")
    private Integer participationId;

    @Column(name = "participant_type", length = 10)
    @Comment("참여자 타입(INTERNAL/EXTERNAL)")
    private String participantType; // INTERNAL | EXTERNAL

    // 참여자 유저 아이디
    @Column(name = "participant_user_id")
    @Comment("참여자 ID")
    private Integer participantUserId;
    
    @Column(name = "participant_name")
    @Comment("참여자 이름")
    private String participantName;

    /** 외부 참여자 전용 필드 */
    @Column(name = "company")
    @Comment("회사")
    private String company;

    @Column(name = "title")
    @Comment("직책")
    private String position;

    @Column(name = "phone")
    @Comment("전화번호")
    private String phone;

    @Column(name = "department")
    @Comment("부서")
    private String department;

    @Column(name = "team")
    @Comment("팀")
    private String team;

    /**
     * N:1 (Receipt)
     * receipt_participants 테이블에 Receipt_ID 존재 (FK)
     */
    @ManyToOne
    @JoinColumn(name = "receipt_id", referencedColumnName = "receipt_id", nullable = false)
    @Comment("영수증 ID")
    @JsonBackReference("receipt_participants")
    private Receipt receipt;
}
