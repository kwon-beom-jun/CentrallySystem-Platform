package com.cs.hrm.service.impl;

import com.cs.core.handler.GlobalExceptionHandler;
import com.cs.hrm.entity.HrmPerformance;
import com.cs.hrm.repository.HrmPerformanceRepository;
import com.cs.hrm.service.HrmPerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HrmPerformanceServiceImpl implements HrmPerformanceService {

    private final HrmPerformanceRepository performanceRepository;

    /**
     * 사용자 id를 이용해 성과 데이터를 조회합니다.
     */
    @Override
    public List<HrmPerformance> getPerformancesByUserId(Integer userId) {
        return performanceRepository.findByUserId(userId);
    }
    
    /**
     * 특정 사용자에서 특정 성과를 삭제합니다.
     * 사용자 id와 성과 id가 일치하는 경우에만 삭제합니다.
     *
     * @param userId 사용자 id
     * @param performanceId 삭제할 성과의 id
     */
    @Override
    public void deletePerformance(Integer userId, Integer performanceId) {
        Optional<HrmPerformance> optPerformance = performanceRepository.findById(performanceId);
        if (optPerformance.isPresent()) {
            HrmPerformance performance = optPerformance.get();
            if (performance.getUserId().equals(userId)) {
                performanceRepository.delete(performance);
            } else {
                throw new RuntimeException(GlobalExceptionHandler.CC + "사용자 id가 일치하지 않습니다.");
            }
        } else {
            throw new RuntimeException(GlobalExceptionHandler.CC + "해당 성과를 찾을 수 없습니다.");
        }
    }
    
    /**
     * 특정 사용자에 새로운 성과 데이터를 추가합니다.
     *
     * @param userId 사용자 id
     * @param performance 추가할 성과 데이터 (userId는 무시하고 경로의 userId로 설정)
     * @return 추가된 성과 데이터
     */
    @Override
    public HrmPerformance createPerformance(Integer userId, HrmPerformance performance) {
        performance.setUserId(userId);
        return performanceRepository.save(performance);
    }
    
    /**
     * 특정 사용자에서 특정 성과 데이터를 수정합니다.
     */
    @Override
    public HrmPerformance patchPerformance(Integer userId, Integer performanceId, HrmPerformance updatedPerformance) {
        Optional<HrmPerformance> optPerformance = performanceRepository.findById(performanceId);
        if (!optPerformance.isPresent()) {
            throw new RuntimeException(GlobalExceptionHandler.CC + "해당 성과를 찾을 수 없습니다.");
        }
        HrmPerformance existingPerformance = optPerformance.get();
        if (!existingPerformance.getUserId().equals(userId)) {
            throw new RuntimeException(GlobalExceptionHandler.CC + "사용자 id가 일치하지 않습니다.");
        }
        // 수정할 필드 업데이트 (필요에 따라 부분 업데이트 가능)
        existingPerformance.setFromDate(updatedPerformance.getFromDate());
        existingPerformance.setToDate(updatedPerformance.getToDate());
        existingPerformance.setWorkType(updatedPerformance.getWorkType());
        existingPerformance.setPerformanceTitle(updatedPerformance.getPerformanceTitle());
        existingPerformance.setPerformance(updatedPerformance.getPerformance());
        existingPerformance.setStatus(updatedPerformance.getStatus());
        return performanceRepository.save(existingPerformance);
    }
}

