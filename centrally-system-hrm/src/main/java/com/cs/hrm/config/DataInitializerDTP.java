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

    // ============================================
    // 더미데이터 초기화 로직 제거됨 (이력용 포트폴리오)
    // ============================================
    // @Bean
    // @Order(1)
    // CommandLineRunner initHrmData_DTP() {
    //     return args -> {
    //         // 더미데이터 생성 로직이 제거되었습니다.
    //     };
    // }
}
