package com.cs.hrm.config;

import com.cs.hrm.entity.HrmDepartments;
import com.cs.hrm.entity.HrmEmploymentType;
import com.cs.hrm.entity.HrmPositions;
import com.cs.hrm.entity.HrmTeams;
import com.cs.hrm.repository.HrmDepartmentsRepository;
import com.cs.hrm.repository.HrmEmploymentTypeRepository;
import com.cs.hrm.repository.HrmPositionsRepository;
import com.cs.hrm.repository.HrmTeamsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * department(D), team(T), position(P) 더미 데이터
 */
@Configuration
@RequiredArgsConstructor
public class DataInitializerDTP {

    private final HrmDepartmentsRepository departmentsRepository;
    private final HrmTeamsRepository teamsRepository;
    private final HrmPositionsRepository positionsRepository;
    private final HrmEmploymentTypeRepository employmentTypeRepository;

    /**
     * 포트폴리오용: 데이터 초기화 기능 비활성화
     */
}
