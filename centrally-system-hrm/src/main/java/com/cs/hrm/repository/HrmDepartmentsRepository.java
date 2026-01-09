package com.cs.hrm.repository;

import com.cs.hrm.entity.HrmDepartments;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HrmDepartmentsRepository extends JpaRepository<HrmDepartments, Integer> {
	
	// teamId를 기준으로 해당 부서를 찾음
    @Query("SELECT d FROM HrmDepartments d JOIN d.teams t WHERE t.teamId = :teamId")
    HrmDepartments findByTeamId(@Param("teamId") Integer teamId);

    /* 활성-부서 중 같은 이름 존재 여부  (soft-delete 고려) */
    boolean existsByDepartmentNameAndEnabledTrue(String departmentName);
    
    /**
     * 부서 조회 시 팀 정보를 함께 페치 조인하여 N+1 문제를 방지하는 메서드
     * LEFT JOIN FETCH를 사용하여 팀이 없는 부서도 조회되도록 합니다.
     */
    @Query("SELECT DISTINCT d FROM HrmDepartments d LEFT JOIN FETCH d.teams t WHERE d.enabled = true")
    List<HrmDepartments> findAllWithTeams();
}
