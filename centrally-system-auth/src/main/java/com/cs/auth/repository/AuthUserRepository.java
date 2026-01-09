package com.cs.auth.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.cs.auth.entity.AuthUser;

import java.util.Optional;

/**
 * 유저 정보를 DB에서 조회하기 위한 JpaRepository
 */
public interface AuthUserRepository extends JpaRepository<AuthUser, Integer> {
	
    Optional<AuthUser> findByEmail(String email);
    
    // 주어진 이메일이 존재하는지 확인하는 메서드
    boolean existsByEmail(String email);
    
    // 팀 아이디로 유저의 팀 아이디를 검색해서 0으로 업데이트
//    @Modifying
//    @Query("UPDATE AuthUser u SET u.teamId = 0 WHERE u.teamId = ?1")
//    void updateTeamIdToZeroByTeamId(Integer teamId);
    
}
