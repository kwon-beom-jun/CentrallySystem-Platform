package com.cs.hrm.repository;

import com.cs.hrm.controller.response.UserStyleCodeView;
import com.cs.hrm.entity.HrmUserStyle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HrmUserStyleRepository extends JpaRepository<HrmUserStyle, Long> {
    
    /**
     * 사용자 ID로 모든 스타일 조회
     */
    List<HrmUserStyle> findByUserId(Integer userId);
    
    /**
     * 사용자 ID와 카테고리로 스타일 조회
     */
    Optional<HrmUserStyle> findByUserIdAndStyleCategory(Integer userId, String styleCategory);
    
    /**
     * 사용자 ID로 스타일 삭제
     */
    void deleteByUserId(Integer userId);
    
    /**
     * 사용자별 스타일 코드 맵 조회
     */
    @Query("""
        SELECT us.styleCategory AS styleCategory, 
               s.styleCode AS styleCode
        FROM HrmUserStyle us
        JOIN us.style s
        WHERE us.userId = :userId
        """)
    List<UserStyleCodeView> findStyleCodesByUserId(@Param("userId") Integer userId);
}

