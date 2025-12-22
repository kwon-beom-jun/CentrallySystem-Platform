package com.cs.hrm.repository;

import com.cs.hrm.entity.HrmStyle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HrmStyleRepository extends JpaRepository<HrmStyle, Integer> {
    
    /**
     * 카테고리별 스타일 조회
     */
    List<HrmStyle> findByStyleCategory(String styleCategory);
    
    /**
     * 카테고리와 코드로 스타일 조회
     */
    Optional<HrmStyle> findByStyleCategoryAndStyleCode(String styleCategory, String styleCode);
}

