package com.cs.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cs.auth.entity.AuthTempUser;
import com.cs.auth.entity.AuthUser;

public interface AuthTempUserRepository extends JpaRepository<AuthTempUser, Integer> {

    Optional<AuthTempUser> findByEmail(String email);
    
    // 주어진 이메일이 존재하는지 확인하는 메서드
    boolean existsByEmail(String email);
}
