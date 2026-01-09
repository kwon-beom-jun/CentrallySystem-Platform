package com.cs.hrm.config;

import com.cs.hrm.entity.HrmPerformance;
import com.cs.hrm.repository.HrmPerformanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializerPerformance {

    private final HrmPerformanceRepository performanceRepository;

    /**
     * 포트폴리오용: 데이터 초기화 기능 비활성화
     */
}
