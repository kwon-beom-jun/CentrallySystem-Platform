package com.cs.auth.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cs.auth.entity.AuthRoles;

public interface AuthRolesRepository extends JpaRepository<AuthRoles, Integer> {
	
	// "ROLE_..." 이름으로 Role 검색
    Optional<AuthRoles> findByRoleName(String roleName);
    
}
