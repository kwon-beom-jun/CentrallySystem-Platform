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

    // ============================================
    // 더미데이터 초기화 로직 제거됨 (이력용 포트폴리오)
    // ============================================
    // @Bean
    // CommandLineRunner initHrmPerformanceData() {
    //     return args -> {
    //         // 더미데이터 생성 로직이 제거되었습니다.
    //     };
    // }
}
