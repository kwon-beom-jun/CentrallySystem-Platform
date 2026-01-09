package com.cs.hrm.config;

import com.cs.hrm.entity.HrmStyle;
import com.cs.hrm.enums.StyleCategory;
import com.cs.hrm.enums.MainCardStyle;
import com.cs.hrm.enums.InfoMobileStyle;
import com.cs.hrm.repository.HrmStyleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * 스타일 초기 데이터 설정
 */
@Configuration
@RequiredArgsConstructor
public class DataInitializerStyles {

    private final HrmStyleRepository styleRepository;

    /**
     * 포트폴리오용: 데이터 초기화 기능 비활성화
     */
}

