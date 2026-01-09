package com.cs.auth.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.cs.auth.entity.AuthSocialLogin;

public interface AuthSocialLoginRepository extends JpaRepository<AuthSocialLogin, Integer> {
	
    // socialName과 socialUserId를 기준으로 AuthSocialLogin을 조회
    Optional<AuthSocialLogin> findBySocialNameAndSocialUserId(String socialName, String socialUserId);
    
    // userId로 소셜 로그인 정보 전체 조회 (유저별 소셜 계정은 여러 개일 수 있으므로 List 반환)
    List<AuthSocialLogin> findByUserId(Integer userId);
    
    // userId와 socialName 조합으로 소셜 로그인 정보 조회
    Optional<AuthSocialLogin> findByUserIdAndSocialName(Integer userId, String socialName);
    
    // 쿼리로 직접 삭제
    @Modifying
    @Query("DELETE FROM AuthSocialLogin sl WHERE sl.userId = :userId AND sl.socialName = :socialName")
    void deleteByUserIdAndSocialName(Integer userId, String socialName);
}
