package com.cs.hrm.service.impl;

import com.cs.core.handler.GlobalExceptionHandler;
import com.cs.hrm.entity.HrmStyle;
import com.cs.hrm.entity.HrmUserStyle;
import com.cs.hrm.enums.StyleCategory;
import com.cs.hrm.enums.MainCardStyle;
import com.cs.hrm.enums.InfoMobileStyle;
import com.cs.hrm.repository.HrmStyleRepository;
import com.cs.hrm.repository.HrmUserStyleRepository;
import com.cs.hrm.service.HrmStyleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HrmStyleServiceImpl implements HrmStyleService {

    private final HrmStyleRepository styleRepository;
    private final HrmUserStyleRepository userStyleRepository;

    /**
     * 모든 스타일 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<HrmStyle> getAllStyles() {
        return styleRepository.findAll();
    }

    /**
     * 카테고리별 스타일 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<HrmStyle> getStylesByCategory(String category) {
        return styleRepository.findByStyleCategory(category);
    }

    /**
     * 사용자의 모든 스타일 설정 조회 (Map 형태)
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getUserStyleCodes(Integer userId) {
        Map<String, String> result = new HashMap<>();
        List<HrmUserStyle> userStyles = userStyleRepository.findByUserId(userId);
        
        userStyles.forEach(us -> {
            result.put(us.getStyleCategory(), us.getStyle().getStyleCode());
        });
        
        // 기본값 설정
        result.putIfAbsent(StyleCategory.MAIN_CARD.getCode(), MainCardStyle.DEFAULT.getCode());
        result.putIfAbsent(StyleCategory.INFO_MOBILE.getCode(), InfoMobileStyle.LIGHT.getCode());
        
        return result;
    }

    /**
     * 사용자 스타일 업데이트 (카테고리별)
     */
    @Override
    @Transactional
    public void updateUserStyle(Integer userId, String category, String styleCode) {
        // 0. 카테고리 유효성 검증
        StyleCategory styleCategory = StyleCategory.from(category);
        if (styleCategory == null) {
            throw new IllegalArgumentException(GlobalExceptionHandler.CC + "존재하지 않는 스타일 카테고리입니다: " + category);
        }
        
        // 0-1. 스타일 코드 유효성 검증 (카테고리별)
        validateStyleCode(styleCategory, styleCode);
        
        // 1. 스타일 마스터에서 조회
        HrmStyle style = styleRepository.findByStyleCategoryAndStyleCode(category, styleCode)
            .orElseThrow(() -> new IllegalArgumentException(GlobalExceptionHandler.CC + "존재하지 않는 스타일입니다: " + category + "." + styleCode));
        
        // 2. 기존 사용자 스타일 조회 또는 생성
        HrmUserStyle userStyle = userStyleRepository
            .findByUserIdAndStyleCategory(userId, category)
            .orElse(HrmUserStyle.builder()
                .userId(userId)
                .styleCategory(category)
                .build());
        
        // 3. 스타일 업데이트
        userStyle.setStyle(style);
        userStyleRepository.save(userStyle);
    }
    
    /**
     * 스타일 코드 유효성 검증
     */
    private void validateStyleCode(StyleCategory category, String styleCode) {
        switch (category) {
            case MAIN_CARD:
                if (MainCardStyle.from(styleCode) == null) {
                    throw new IllegalArgumentException(GlobalExceptionHandler.CC + "유효하지 않은 메인 카드 스타일 코드입니다: " + styleCode);
                }
                break;
            case INFO_MOBILE:
                if (InfoMobileStyle.from(styleCode) == null) {
                    throw new IllegalArgumentException(GlobalExceptionHandler.CC + "유효하지 않은 기본 정보 모바일 스타일 코드입니다: " + styleCode);
                }
                break;
        }
    }

    /**
     * 메인 카드 스타일 업데이트 (기존 API 호환용)
     */
    @Override
    @Transactional
    public void updateMainCardStyle(Integer userId, String styleCode) {
        updateUserStyle(userId, StyleCategory.MAIN_CARD.getCode(), styleCode);
    }
    
    /**
     * 기본 정보 모바일 스타일 업데이트
     */
    @Override
    @Transactional
    public void updateInfoMobileStyle(Integer userId, String styleCode) {
        updateUserStyle(userId, StyleCategory.INFO_MOBILE.getCode(), styleCode);
    }
}

