package com.cs.auth.entity;

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
@Table(name = "temp_user_agreements")
public class TempUserAgreement extends AuditTimeEntity {

    // 약관 동의 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "temp_user_agreement_id")
    @Comment("약관 동의 ID")
    private Long tempUserAgreementId;

    // 임시 회원 이메일
    @Column(name = "user_email", nullable = false)
    @Comment("임시 회원 이메일")
    private String userEmail;

    // 임시 회원 이름
    @Column(name = "user_name", nullable = false)
    @Comment("임시 회원 이름")
    private String userName;

    // 약관 ID (Agreement FK)
    @Column(name = "agreement_id", nullable = false)
    @Comment("약관 ID (Agreement FK)")
    private Long agreementId;

    // 동의 여부
    @Column(name = "is_agreed", nullable = false)
    @Comment("동의 여부")
    private Boolean isAgreed;
} 