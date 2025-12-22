package com.cs.auth.repository;

import com.cs.auth.entity.AuthRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Refresh Token Repository
 */
@Repository
public interface AuthRefreshTokenRepository extends JpaRepository<AuthRefreshToken, Long> {

    /**
     * Token 값으로 조회
     */
    Optional<AuthRefreshToken> findByTokenValue(String tokenValue);

    /**
     * 사용자 ID로 모든 토큰 조회
     */
    List<AuthRefreshToken> findAllByUserId(Integer userId);

    /**
     * 사용자 ID로 토큰 개수 조회
     */
    Long countByUserId(Integer userId);

    /**
     * Token 값으로 삭제
     */
    @Transactional
    @Modifying
    void deleteByTokenValue(String tokenValue);

    /**
     * 사용자 ID로 모든 토큰 삭제 (보안 위협 시)
     */
    @Transactional
    @Modifying
    void deleteAllByUserId(Integer userId);

    /**
     * 만료된 토큰 삭제 (배치 작업용)
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM AuthRefreshToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * 사용자의 가장 오래된 토큰 조회
     */
    @Query("SELECT rt FROM AuthRefreshToken rt WHERE rt.userId = :userId ORDER BY rt.createdAt ASC")
    List<AuthRefreshToken> findOldestByUserId(@Param("userId") Integer userId);

    /**
     * 특정 시간 이전에 생성된 토큰 삭제
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM AuthRefreshToken rt WHERE rt.userId = :userId AND rt.createdAt < :beforeDate")
    void deleteOldTokensByUserId(
        @Param("userId") Integer userId, 
        @Param("beforeDate") LocalDateTime beforeDate);
}

