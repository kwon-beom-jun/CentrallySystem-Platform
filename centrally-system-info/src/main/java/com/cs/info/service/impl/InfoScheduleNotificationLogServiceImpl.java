package com.cs.info.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cs.core.vo.page.PageResponseVo;
import com.cs.info.controller.response.ScheduleNotificationLogResponse;
import com.cs.info.entity.InfoScheduleNotificationLog;
import com.cs.info.entity.InfoSchedule;
import com.cs.info.repository.InfoScheduleNotificationLogRepository;
import com.cs.info.repository.InfoScheduleRepository;
import com.cs.info.service.InfoScheduleNotificationLogService;

import lombok.extern.slf4j.Slf4j;

/**
 * 일정 알림 발송 이력 서비스 구현체
 */
@Service
@Slf4j
public class InfoScheduleNotificationLogServiceImpl implements InfoScheduleNotificationLogService {

    @Autowired
    private InfoScheduleNotificationLogRepository logRepository;
    
    @Autowired
    private InfoScheduleRepository scheduleRepository;

    /**
     * 알림 발송 이력 페이징 조회
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponseVo<ScheduleNotificationLogResponse> getNotificationHistory(
            LocalDate startDate,
            LocalDate endDate,
            String searchKeyword,
            int page,
            int size) {
        
        // 날짜를 LocalDateTime으로 변환 (시작일은 00:00:00, 종료일은 23:59:59)
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // 페이징 및 정렬 설정
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "scheduledTime"));

        // Repository에서 페이징 조회
        Page<InfoScheduleNotificationLog> logPage;
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            // 사용자명 또는 이메일로 검색
            logPage = logRepository.findByDateRangeAndUserNameOrEmail(
                    startDateTime,
                    endDateTime,
                    searchKeyword.trim(),
                    pageable
            );
        } else {
            // 검색어가 없으면 전체 조회
            logPage = logRepository.findByDateRangeAndUserId(
                    startDateTime,
                    endDateTime,
                    null,
                    pageable
            );
        }

        // 일정 ID 목록 수집
        List<Long> scheduleIds = logPage.getContent().stream()
                .map(InfoScheduleNotificationLog::getScheduleId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        // 일정 정보 배치 조회
        final Map<Long, InfoSchedule> scheduleMap;
        if (!scheduleIds.isEmpty()) {
            List<InfoSchedule> schedules = scheduleRepository.findAllById(scheduleIds);
            scheduleMap = schedules.stream()
                    .collect(Collectors.toMap(InfoSchedule::getScheduleId, s -> s));
        } else {
            scheduleMap = new HashMap<>();
        }
        
        // Entity를 Response DTO로 변환
        List<ScheduleNotificationLogResponse> responses = logPage.getContent().stream()
                .<ScheduleNotificationLogResponse>map(log -> {
                    // 시간에서 초를 00으로 설정
                    LocalDateTime scheduledTime = log.getScheduledTime() != null 
                        ? log.getScheduledTime().withSecond(0).withNano(0) 
                        : null;
                    LocalDateTime sentTime = log.getSentTime() != null 
                        ? log.getSentTime().withSecond(0).withNano(0) 
                        : null;
                    
                    // 알림 유형과 데이터값 분리
                    NotificationTypeInfo typeInfo = parseNotificationType(log.getNotificationType());
                    
                    // 일정 정보에서 시작/종료 시간 가져오기
                    LocalDateTime scheduleStartTime = null;
                    LocalDateTime scheduleEndTime = null;
                    if (log.getScheduleId() != null) {
                        InfoSchedule schedule = scheduleMap.get(log.getScheduleId());
                        if (schedule != null) {
                            scheduleStartTime = schedule.getStartTime();
                            scheduleEndTime = schedule.getEndTime();
                        }
                    }
                    
                    return ScheduleNotificationLogResponse.builder()
                            .logId(log.getLogId())
                            .scheduleId(log.getScheduleId())
                            .userId(log.getUserId())
                            .userName(log.getUserName())
                            .userEmail(log.getUserEmail())
                            .notificationType(typeInfo.type)
                            .notificationValue(typeInfo.value)
                            .scheduledTime(scheduledTime)
                            .sentTime(sentTime)
                            .scheduleStartTime(scheduleStartTime)
                            .scheduleEndTime(scheduleEndTime)
                            .channel(log.getChannel())
                            .sent(log.getSent())
                            .failureReason(log.getFailureReason())
                            .build();
                })
                .collect(Collectors.toList());

        // PageImpl을 사용하여 PageResponseVo 생성
        Page<ScheduleNotificationLogResponse> responsePage = new PageImpl<>(
                responses,
                logPage.getPageable(),
                logPage.getTotalElements()
        );

        return new PageResponseVo<>(responsePage);
    }

    /**
     * 알림 유형 정보 (내부 클래스)
     */
    private static class NotificationTypeInfo {
        String type;  // "[DAY]", "[HOUR]", "[MINUTE]"
        String value; // "2025-12-19 16:13", "1시간 1분 전", "59분 전"
    }

    /**
     * 저장된 알림 유형 문자열을 파싱하여 알림 유형과 데이터값으로 분리
     * 
     * @param notificationType 저장된 알림 유형 문자열
     * @return 알림 유형 정보
     */
    private NotificationTypeInfo parseNotificationType(String notificationType) {
        NotificationTypeInfo info = new NotificationTypeInfo();
        
        if (notificationType == null || notificationType.trim().isEmpty()) {
            info.type = "";
            info.value = "";
            return info;
        }

        String trimmed = notificationType.trim();

        // "YYYY-MM-DD HH:MM" 형식인지 확인 (예: "2025-12-19 16:13")
        Pattern dateTimePattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$");
        if (dateTimePattern.matcher(trimmed).matches()) {
            info.type = "[DAY]";
            info.value = trimmed;
            return info;
        }

        // "해당 일자 알림 (YYYY-MM-DD HH:MM)" 형식인지 확인
        Pattern dayPattern = Pattern.compile("^해당 일자 알림 \\((\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2})\\)$");
        java.util.regex.Matcher dayMatcher = dayPattern.matcher(trimmed);
        if (dayMatcher.matches()) {
            info.type = "[DAY]";
            info.value = dayMatcher.group(1);
            return info;
        }

        // "N시간 N분 전" 또는 "N시간 전" 형식인지 확인
        Pattern hourPattern = Pattern.compile("^(\\d+)시간( (\\d+)분)? 전$");
        java.util.regex.Matcher hourMatcher = hourPattern.matcher(trimmed);
        if (hourMatcher.matches()) {
            info.type = "[HOUR]";
            info.value = trimmed;
            return info;
        }

        // "N분 전" 형식인지 확인
        Pattern minutePattern = Pattern.compile("^(\\d+)분 전$");
        java.util.regex.Matcher minuteMatcher = minutePattern.matcher(trimmed);
        if (minuteMatcher.matches()) {
            info.type = "[MINUTE]";
            info.value = trimmed;
            return info;
        }

        // 기존 형식 "N일 전", "N시간 전", "N분 전" 변환
        if (trimmed.contains("일 전")) {
            info.type = "[DAY]";
            info.value = trimmed;
            return info;
        } else if (trimmed.contains("시간 전")) {
            info.type = "[HOUR]";
            info.value = trimmed;
            return info;
        } else if (trimmed.contains("분 전")) {
            info.type = "[MINUTE]";
            info.value = trimmed;
            return info;
        }

        // 알 수 없는 형식이면 그대로 반환
        info.type = "";
        info.value = trimmed;
        return info;
    }
}

