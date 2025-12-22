package com.cs.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

import com.cs.core.entity.AuditTimeEntity;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "auth_email_code")
public class AuthEmailCode extends AuditTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_id")
    @Comment("이메일 아이디")
    private Integer emailId;

    @Column(name = "email", nullable = false, unique = true)
    @Comment("이메일")
    private String email;

    @Column(name = "verification_code", length = 10)
    @Comment("인증 코드")
    private String verificationCode;

    @Column(name = "created_at")
    @Comment("생성 시간")
    private Timestamp createdAt;

    @Column(name = "expires_at")
    @Comment("만료 시간")
    private Timestamp expiresAt;

    @Column(name = "verified")
    @Comment("인증 여부")
    private Boolean verified;
}
