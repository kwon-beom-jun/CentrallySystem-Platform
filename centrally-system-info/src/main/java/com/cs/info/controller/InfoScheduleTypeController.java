package com.cs.info.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cs.info.entity.InfoScheduleType;
import com.cs.info.service.InfoScheduleTypeService;

/**
 * 일정 유형 컨트롤러
 */
@RestController
@RequestMapping("/schedule-types")
public class InfoScheduleTypeController {

    @Autowired
    private InfoScheduleTypeService infoScheduleTypeService;

    /**
     * 일정 유형 목록 조회 (관리 페이지용, isActive 필터링 없음)
     */
    @GetMapping
    public ResponseEntity<List<InfoScheduleType>> getAllScheduleTypes() {
        List<InfoScheduleType> scheduleTypes = infoScheduleTypeService.getAllScheduleTypes();
        return ResponseEntity.ok(scheduleTypes);
    }

    /**
     * 캘린더용 일정 유형 목록 조회 (enabled=true인 모든 항목, isActive 필터링 없음)
     */
    @GetMapping("/for-calendar")
    public ResponseEntity<List<InfoScheduleType>> getAllScheduleTypesForCalendar() {
        List<InfoScheduleType> scheduleTypes = infoScheduleTypeService.getAllScheduleTypesForCalendar();
        return ResponseEntity.ok(scheduleTypes);
    }

    /**
     * 일정 유형 단건 조회
     */
    @GetMapping("/{scheduleTypeId}")
    public ResponseEntity<InfoScheduleType> getScheduleType(@PathVariable("scheduleTypeId") Long scheduleTypeId) {
        InfoScheduleType scheduleType = infoScheduleTypeService.getScheduleType(scheduleTypeId);
        return ResponseEntity.ok(scheduleType);
    }

    /**
     * 코드로 일정 유형 조회
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<InfoScheduleType> getScheduleTypeByCode(@PathVariable("code") String code) {
        InfoScheduleType scheduleType = infoScheduleTypeService.getScheduleTypeByCode(code);
        return ResponseEntity.ok(scheduleType);
    }

    /**
     * 일정 유형 생성
     */
    @PostMapping
    public ResponseEntity<InfoScheduleType> createScheduleType(@RequestBody InfoScheduleType scheduleType) {
        InfoScheduleType saved = infoScheduleTypeService.createScheduleType(scheduleType);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * 일정 유형 수정
     */
    @PutMapping("/{scheduleTypeId}")
    public ResponseEntity<InfoScheduleType> updateScheduleType(
            @PathVariable("scheduleTypeId") Long scheduleTypeId,
            @RequestBody InfoScheduleType scheduleType) {
        InfoScheduleType updated = infoScheduleTypeService.updateScheduleType(scheduleTypeId, scheduleType);
        return ResponseEntity.ok(updated);
    }

    /**
     * 일정 유형 삭제
     */
    @DeleteMapping("/{scheduleTypeId}")
    public ResponseEntity<Void> deleteScheduleType(@PathVariable("scheduleTypeId") Long scheduleTypeId) {
        infoScheduleTypeService.deleteScheduleType(scheduleTypeId);
        return ResponseEntity.noContent().build();
    }
}

