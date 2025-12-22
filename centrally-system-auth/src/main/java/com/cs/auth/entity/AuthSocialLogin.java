package com.cs.auth.entity;

import com.cs.core.entity.AuditTimeEntity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "auth_social_login",
    // socialName + socialUserId 복합 유니크 제약
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"social_name", "social_user_id"})
    }
)
public class AuthSocialLogin extends AuditTimeEntity {

    // PK: 자동 증가
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "social_id")
    @Comment("소셜 ID")
    private Integer socialId;

    // 프로바이더 이름 (ex: "kakao", "google")
    @Column(name = "social_name", nullable = false)
    @Comment("소셜 이름")
    private String socialName;

    // 소셜에서의 고유 사용자 식별자
    @Column(name = "social_user_id", nullable = false)
    @Comment("소셜 사용자 ID")
    private String socialUserId;

    // 우리 시스템의 User_ID (auth_users) 참조
    @Column(name = "user_id", nullable = false)
    @Comment("사용자 ID")
    private Integer userId;

    // 소셜에서 제공된 이메일 (필요에 따라 null 허용)
    @Column(name = "social_email")
    @Comment("소셜 이메일")
    private String socialEmail;
}
