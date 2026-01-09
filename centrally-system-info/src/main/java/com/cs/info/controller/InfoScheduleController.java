package com.cs.info.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.cs.core.security.CustomUserPrincipal;
import com.cs.info.controller.request.ScheduleCreateRequest;
import com.cs.info.controller.request.ScheduleUpdateRequest;
import com.cs.info.controller.response.ScheduleResponse;
import com.cs.info.entity.InfoSchedule;
import com.cs.info.service.InfoScheduleService;
import com.cs.info.util.RoleUtils;

/**
 * 일정 컨트롤러
 */
@RestController
@RequestMapping("/schedules")
public class InfoScheduleController {

    @Autowired
    private InfoScheduleService infoScheduleService;

    /**
     * 일정 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getSchedules(
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "departmentId", required = false) Integer departmentId,
            @RequestParam(name = "assigneeId", required = false) Integer assigneeId) {
        
        // 기본값: 현재 월의 시작일과 종료일
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        }
        
        List<InfoSchedule> schedules = infoScheduleService.getSchedules(startDate, endDate, departmentId, assigneeId);
        List<ScheduleResponse> responses = schedules.stream()
                .map(ScheduleResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * 일정 단건 조회
     */
    @GetMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponse> getSchedule(@PathVariable("scheduleId") Long scheduleId) {
        InfoSchedule schedule = infoScheduleService.getSchedule(scheduleId);
        return ResponseEntity.ok(ScheduleResponse.from(schedule));
    }

    /**
     * 일정 생성
     */
    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(@RequestBody ScheduleCreateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserPrincipal principal = (CustomUserPrincipal) auth.getPrincipal();
        
        InfoSchedule saved = infoScheduleService.createSchedule(
            request, 
            principal.getUserId(), 
            principal.getUsername()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ScheduleResponse.from(saved));
    }

    /**
     * 일정 수정
     */
    @PutMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponse> updateSchedule(
            @PathVariable("scheduleId") Long scheduleId,
            @RequestBody ScheduleUpdateRequest request) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserPrincipal principal = (CustomUserPrincipal) auth.getPrincipal();
        boolean isManager = RoleUtils.isManager(auth.getAuthorities());
        
        InfoSchedule updated = infoScheduleService.updateSchedule(
            scheduleId, 
            request, 
            principal.getUserId(), 
            isManager
        );
        return ResponseEntity.ok(ScheduleResponse.from(updated));
    }

    /**
     * 일정 삭제
     */
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable("scheduleId") Long scheduleId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserPrincipal principal = (CustomUserPrincipal) auth.getPrincipal();
        boolean isManager = RoleUtils.isManager(auth.getAuthorities());
        
        infoScheduleService.deleteSchedule(scheduleId, principal.getUserId(), isManager);
        return ResponseEntity.noContent().build();
    }
}

