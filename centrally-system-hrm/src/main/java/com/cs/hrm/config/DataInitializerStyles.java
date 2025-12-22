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

    @Bean
    @Order(0) // 가장 먼저 실행 (다른 데이터에 의존하지 않음)
    public CommandLineRunner initStyles() {
        return args -> {
            if (styleRepository.count() > 0) {
                System.out.println("⏭️  스타일 데이터가 이미 존재합니다.");
                return;
            }

            List<HrmStyle> styles = List.of(
                // 메인 카드 스타일
                HrmStyle.builder()
                    .styleCategory(StyleCategory.MAIN_CARD.getCode())
                    .styleCode(MainCardStyle.DEFAULT.getCode())
                    .styleName(MainCardStyle.DEFAULT.getLabel())
                    .description("기본 메인 페이지 카드 스타일")
                    .build(),
                HrmStyle.builder()
                    .styleCategory(StyleCategory.MAIN_CARD.getCode())
                    .styleCode(MainCardStyle.VER1.getCode())
                    .styleName(MainCardStyle.VER1.getLabel())
                    .description("스타일 1")
                    .build(),
                HrmStyle.builder()
                    .styleCategory(StyleCategory.MAIN_CARD.getCode())
                    .styleCode(MainCardStyle.VER2.getCode())
                    .styleName(MainCardStyle.VER2.getLabel())
                    .description("스타일 2")
                    .build(),
                HrmStyle.builder()
                    .styleCategory(StyleCategory.MAIN_CARD.getCode())
                    .styleCode(MainCardStyle.VER3.getCode())
                    .styleName(MainCardStyle.VER3.getLabel())
                    .description("스타일 3")
                    .build(),
                HrmStyle.builder()
                    .styleCategory(StyleCategory.MAIN_CARD.getCode())
                    .styleCode(MainCardStyle.VER4.getCode())
                    .styleName(MainCardStyle.VER4.getLabel())
                    .description("스타일 4")
                    .build(),
                
                // 기본 정보 모바일 테마
                HrmStyle.builder()
                    .styleCategory(StyleCategory.INFO_MOBILE.getCode())
                    .styleCode(InfoMobileStyle.LIGHT.getCode())
                    .styleName(InfoMobileStyle.LIGHT.getLabel())
                    .description("밝은 테마 (기본)")
                    .build(),
                HrmStyle.builder()
                    .styleCategory(StyleCategory.INFO_MOBILE.getCode())
                    .styleCode(InfoMobileStyle.DARK.getCode())
                    .styleName(InfoMobileStyle.DARK.getLabel())
                    .description("어두운 테마")
                    .build()
            );

            styleRepository.saveAll(styles);
            System.out.println("✅ 스타일 초기 데이터 삽입 완료!");
        };
    }
}

