package com.cs.hrm.service.impl;

import com.cs.core.handler.GlobalExceptionHandler;
import com.cs.hrm.entity.HrmPositions;
import com.cs.hrm.repository.HrmPositionsRepository;
import com.cs.hrm.service.HrmPositionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HrmPositionsServiceImpl implements HrmPositionsService {

    private final HrmPositionsRepository positionsRepository;

    /**
     * 전체 직책 정보 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<HrmPositions> getPositions() {
    	return positionsRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public HrmPositions getPosition(Integer positionId) {
        return positionsRepository.findById(positionId)
                .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "직책 정보를 찾을 수 없습니다. ID=" + positionId));
    }

    /**
     * 포지션 ID로 특정 직책 조회
     */
    @Override
    public Optional<HrmPositions> getPositionById(Integer positionId) {
        return positionsRepository.findByPositionId(positionId);
    }

    /**
     * 직책 삭제
     */
    @Override
    @Transactional
    public void deletePosition(Integer positionId) {
        if (!positionsRepository.existsById(positionId)) {
            throw new RuntimeException(GlobalExceptionHandler.CC + "직책 정보를 찾을 수 없습니다. ID=" + positionId);
        }
        positionsRepository.deleteById(positionId);
    }
}

