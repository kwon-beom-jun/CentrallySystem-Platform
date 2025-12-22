package com.cs.hrm.repository;

import com.cs.hrm.entity.HrmPerformance;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HrmPerformanceRepository extends JpaRepository<HrmPerformance, Integer> {
	
    // 사용자 id로 성과 데이터를 조회하는 메서드 추가
    List<HrmPerformance> findByUserId(Integer userId);
    
}
