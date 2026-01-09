package com.cs.hrm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cs.hrm.entity.HrmRoles;

public interface HrmRolesRepository extends JpaRepository<HrmRoles,Integer> {
    Optional<HrmRoles> findByRoleName(String roleName);
}
