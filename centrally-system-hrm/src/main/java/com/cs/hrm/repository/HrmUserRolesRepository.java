package com.cs.hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cs.hrm.entity.HrmUserRoles;

public interface HrmUserRolesRepository extends JpaRepository<HrmUserRoles,Long> {
    boolean existsByUserUserIdAndRoleRoleId(Integer userId, Integer roleId);
}
