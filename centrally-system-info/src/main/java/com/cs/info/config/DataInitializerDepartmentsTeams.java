package com.cs.info.config;

import com.cs.info.entity.InfoDepartments;
import com.cs.info.entity.InfoTeams;
import com.cs.info.repository.InfoDepartmentsRepository;
import com.cs.info.repository.InfoTeamsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * 부서 및 팀 초기 데이터 생성
 */
@Configuration
@RequiredArgsConstructor
public class DataInitializerDepartmentsTeams {

    private final InfoDepartmentsRepository departmentsRepository;
    private final InfoTeamsRepository teamsRepository;

    /**
     * 포트폴리오용: 데이터 초기화 기능 비활성화
     */
}

