package com.cs.hrm.repository;

import com.cs.hrm.entity.HrmTeams;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HrmTeamsRepository extends JpaRepository<HrmTeams, Integer> {

    /* 같은 부서 안에서, 활성-팀 중 동일 이름 존재 여부 */
    boolean existsByTeamNameAndDepartment_DepartmentIdAndEnabledTrue(String teamName, Integer departmentId);
}
