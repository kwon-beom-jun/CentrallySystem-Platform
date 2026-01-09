package com.cs.info.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.cs.core.security.CustomUserPrincipal;
import com.cs.info.controller.request.ScheduleNotificationUpdateRequest;
import com.cs.info.controller.response.ScheduleNotificationResponse;
import com.cs.info.entity.InfoScheduleNotification;
import com.cs.info.service.InfoScheduleNotificationService;

/**
 * 일정 알림 설정 컨트롤러
 */
@RestController
@RequestMapping("/schedule-notifications")
public class InfoScheduleNotificationController {

    @Autowired
    private InfoScheduleNotificationService notificationService;

    /**
     * 현재 사용자의 알림 설정 조회
     */
    @GetMapping("/me")
    public ResponseEntity<ScheduleNotificationResponse> getMyNotificationSettings() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserPrincipal principal = (CustomUserPrincipal) auth.getPrincipal();
        
        InfoScheduleNotification settings = notificationService.getNotificationSettings(principal.getUserId());
        return ResponseEntity.ok(ScheduleNotificationResponse.from(settings));
    }

    /**
     * 특정 사용자의 알림 설정 조회 (관리자용)
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ScheduleNotificationResponse> getNotificationSettingsByUserId(
            @PathVariable("userId") Integer userId) {
        InfoScheduleNotification settings = notificationService.getNotificationSettings(userId);
        return ResponseEntity.ok(ScheduleNotificationResponse.from(settings));
    }

    /**
     * 알림 설정 저장/수정
     */
    @PutMapping("/me")
    public ResponseEntity<ScheduleNotificationResponse> updateMyNotificationSettings(
            @RequestBody ScheduleNotificationUpdateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserPrincipal principal = (CustomUserPrincipal) auth.getPrincipal();
        
        InfoScheduleNotification notificationSettings = InfoScheduleNotification.builder()
                .notificationTimings(request.getNotificationTimings())
                .timezone(request.getTimezone())
                .weekdayStartTime(request.getWeekdayStartTime())
                .weekdayEndTime(request.getWeekdayEndTime())
                .weekendEnabled(request.getWeekendEnabled())
                .holidayEnabled(request.getHolidayEnabled())
                .scheduleTypeNotifications(request.getScheduleTypeNotifications())
                .emailEnabled(request.getEmailEnabled())
                .smsEnabled(request.getSmsEnabled())
                .pushEnabled(request.getPushEnabled())
                .build();
        
        InfoScheduleNotification saved = notificationService.saveNotificationSettings(
                principal.getUserId(), 
                notificationSettings
        );
        return ResponseEntity.ok(ScheduleNotificationResponse.from(saved));
    }

    /**
     * 알림 설정 삭제
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyNotificationSettings() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserPrincipal principal = (CustomUserPrincipal) auth.getPrincipal();
        
        notificationService.deleteNotificationSettings(principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}

