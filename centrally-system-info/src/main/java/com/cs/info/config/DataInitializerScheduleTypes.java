package com.cs.info.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.cs.info.entity.InfoScheduleType;
import com.cs.info.repository.InfoScheduleTypeRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * 일정 유형 초기 데이터 생성
 */
@Configuration
@Slf4j
public class DataInitializerScheduleTypes {

    @Autowired
    private InfoScheduleTypeRepository infoScheduleTypeRepository;

    /**
     * 일정 유형 Seed 데이터
     */
    private static final List<ScheduleTypeSeed> SCHEDULE_TYPE_SEEDS = List.of(
        new ScheduleTypeSeed(
            "OTHER",
            "기타",
            "#9E9E9E",
            1
        ),
        new ScheduleTypeSeed(
            "BUSINESS_TRIP",
            "외근",
            "#FF9800",
            2
        ),
        new ScheduleTypeSeed(
            "VACATION",
            "휴가",
            "#4CAF50",
            3
        ),
        new ScheduleTypeSeed(
            "MEETING",
            "회의",
            "#2196F3",
            4
        ),
        new ScheduleTypeSeed(
            "EDUCATION",
            "교육",
            "#9C27B0",
            5
        )
    );

    /**
     * 포트폴리오용: 데이터 초기화 기능 비활성화
     */

    /**
     * 일정 유형 Seed 레코드
     */
    private static record ScheduleTypeSeed(
        String code,
        String label,
        String color,
        Integer displayOrder
    ) {}
}

