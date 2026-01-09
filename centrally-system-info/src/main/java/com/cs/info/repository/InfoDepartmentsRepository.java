package com.cs.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cs.info.entity.InfoDepartments;

/**
 * INFO 부서 레포지토리
 */
@Repository
public interface InfoDepartmentsRepository extends JpaRepository<InfoDepartments, Integer> {

    /**
     * 부서명으로 활성 부서 조회
     */
    boolean existsByDepartmentNameAndEnabledTrue(String departmentName);
}

