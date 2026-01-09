package com.cs.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Refresh Token 엔티티
 * Access Token 갱신을 위한 장기 토큰 저장
 */
@Entity
@Table(name = "auth_refresh_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long tokenId;

    /**
     * 사용자 ID (auth_users.user_id 참조)
     */
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    /**
     * Refresh Token 값 (UUID)
     */
    @Column(name = "token_value", nullable = false, unique = true, length = 500)
    private String tokenValue;

    /**
     * 토큰 만료 시간
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * 토큰 생성 시간
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 마지막 사용 시간
     */
    @Column(name = "last_used_at", nullable = false)
    private LocalDateTime lastUsedAt;

    /**
     * 토큰 발급 시 IP 주소
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    /**
     * 토큰 발급 시 User-Agent
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * 토큰 사용 여부 (Rotation 감지용)
     */
    @Column(name = "is_used", nullable = false)
    @Builder.Default
    private Boolean isUsed = false;

    /**
     * 토큰이 만료되었는지 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 토큰을 사용 처리
     */
    public void markAsUsed() {
        this.isUsed = true;
        this.lastUsedAt = LocalDateTime.now();
    }
}

