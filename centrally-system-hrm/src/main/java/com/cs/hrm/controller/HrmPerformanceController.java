package com.cs.hrm.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.cs.hrm.entity.HrmPerformance;
import com.cs.hrm.service.HrmPerformanceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/performances")
public class HrmPerformanceController {
	
    private final HrmPerformanceService performanceService;

    /**
     * 사용자 id를 받아 해당 사용자의 성과 데이터를 반환합니다.
     *
     * @param userId 사용자 id
     * @return 해당 사용자의 성과 데이터 목록
     */
    @GetMapping("/{userId}")
    public List<HrmPerformance> getPerformancesByUserId(@PathVariable("userId") Integer userId) {
        return performanceService.getPerformancesByUserId(userId);
    }
    
    /**
     * 특정 사용자에서 특정 성과를 삭제합니다.
     * 예: DELETE /permissions/123/456
     *  - 123: 사용자 id, 456: 성과 id
     */
    @DeleteMapping("/{userId}/{performanceId}")
    public void deletePerformance(@PathVariable("userId") Integer userId,
                                  @PathVariable("performanceId") Integer performanceId) {
        performanceService.deletePerformance(userId, performanceId);
    }

    /**
     * 특정 사용자에 새로운 성과 데이터를 추가합니다.
     * 예: POST /permissions/123
     *  - 123: 사용자 id
     */
    @PostMapping("/{userId}")
    public HrmPerformance createPerformance(@PathVariable("userId") Integer userId,
                                            @RequestBody HrmPerformance performance) {
        return performanceService.createPerformance(userId, performance);
    }
    
    /**
     * 특정 사용자에서 특정 성과 데이터를 수정합니다.
     * 예: PATCH /permissions/123/456
     *  - 123: 사용자 id, 456: 성과 id
     */
    @PatchMapping("/{userId}/{performanceId}")
    public HrmPerformance patchPerformance(@PathVariable("userId") Integer userId,
                                           @PathVariable("performanceId") Integer performanceId,
                                           @RequestBody HrmPerformance updatedPerformance) {
        return performanceService.patchPerformance(userId, performanceId, updatedPerformance);
    }
	
}
