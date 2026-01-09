package com.cs.auth.repository;

import com.cs.auth.entity.AuthApplication;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthApplicationRepository extends JpaRepository<AuthApplication, Integer> {
	
    boolean existsByApplicationName(String applicationName);
    
    /**
     * 어플리케이션 이름으로 조회
     */
    Optional<AuthApplication> findByApplicationName(String applicationName);
    
}
