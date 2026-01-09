package com.cs.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.Comment;

/**
 * 소셜 연동 임시 토큰을 DB에 저장하기 위한 엔티티
 */
@Entity
@Table(name = "auth_social_link_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthSocialLinkToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "social_link_id")
    private Long id;

    // 랜덤하게 생성되는 토큰 값
    @Column(name = "token_value", nullable = false, unique = true)
    @Comment("토큰 값")
    private String tokenValue;

    // 소셜 종류(google, kakao 등)
    @Column(name = "social_name", nullable = false)
    @Comment("소셜 이름")
    private String socialName;

    // 소셜에서 제공하는 고유 ID (googleSubject, kakaoId 등)
    @Column(name = "social_id", nullable = false)
    @Comment("소셜 고유 ID")
    private String socialId;

    // 소셜에서 가져온 이메일
    @Column(name = "social_email")
    @Comment("소셜 이메일")
    private String socialEmail;

    // 소셜에서 가져온 닉네임 등
    @Column(name = "nickname")
    @Comment("닉네임")
    private String nickname;

    // 토큰 생성 시간
    @Column(name = "created_at", nullable = false)
    @Comment("생성 시간")
    private LocalDateTime createdAt;

    // 토큰 만료 시간 (예: 5분 후)
    @Column(name = "expires_at", nullable = false)
    @Comment("만료 시간")
    private LocalDateTime expiresAt;

    // 이미 사용되었는지 여부 (한 번 사용 시 true로 마킹)
    @Column(name = "used", nullable = false)
    @Comment("사용 여부")
    private boolean used;
    
    /**
     * 만료되었거나 이미 사용되었는지 판별
     */
    public boolean isInvalid() {
        return used || LocalDateTime.now().isAfter(expiresAt);
    }
}
