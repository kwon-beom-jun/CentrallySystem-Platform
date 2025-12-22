package com.cs.auth.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cs.auth.entity.AuthEmailCode;

@Repository
public interface AuthEmailCodeRepository extends JpaRepository<AuthEmailCode, Integer> {
	
	Optional<AuthEmailCode> findByEmail(String email);
	
}
