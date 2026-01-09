package com.cs.hrm.repository;

import com.cs.hrm.entity.HrmPositions;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HrmPositionsRepository extends JpaRepository<HrmPositions, Integer> {
	
	// 포지션 ID로 포지션 정보를 조회
    Optional<HrmPositions> findByPositionId(Integer positionId);

}
