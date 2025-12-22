package com.cs.hrm.controller;

import com.cs.hrm.entity.HrmPositions;
import com.cs.hrm.service.HrmPositionsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/positions")
public class HrmPositionsController {

    private final HrmPositionsService positionsService;

    public HrmPositionsController(HrmPositionsService positionsService) {
        this.positionsService = positionsService;
    }

    /**
     * 전체 직책 정보를 조회하는 API 엔드포인트입니다.
     *
     * @return 직책 목록(직책 정보 포함)
     */
    @GetMapping
    public ResponseEntity<List<HrmPositions>> getPositions() {
        List<HrmPositions> positions = positionsService.getPositions();
        return ResponseEntity.ok(positions);
    }

    /**
     * 포지션 ID로 특정 직책 조회
     */
    @GetMapping("/{positionId}")
    public ResponseEntity<HrmPositions> getPositionById(@PathVariable("positionId") Integer positionId) {
        Optional<HrmPositions> position = positionsService.getPositionById(positionId);
        return position.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
