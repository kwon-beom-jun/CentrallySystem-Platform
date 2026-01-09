package com.cs.info.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cs.info.entity.InfoSchedule;
import com.cs.info.entity.InfoScheduleNotification;
import com.cs.info.enums.NotificationTimingUnit;
import com.cs.info.repository.InfoScheduleNotificationRepository;
import com.cs.info.repository.InfoScheduleRepository;
import com.cs.info.service.ScheduleNotificationService;
import com.cs.info.util.NotificationTimingCalculator;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 일정 알림 스케줄러
 * 5분마다 실행하여 알림 발송 대상 일정을 확인하고 발송
 */
@Component
@Slf4j
public class ScheduleNotificationScheduler {

    @Autowired
    private InfoScheduleRepository scheduleRepository;

    @Autowired
    private InfoScheduleNotificationRepository notificationRepository;

    @Autowired
    private ScheduleNotificationService notificationService;

    @Autowired
    private NotificationTimingCalculator timingCalculator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 알림 발송 스케줄러
     * 1분마다 실행 (cron: 초 분 시 일 월 요일)
     */
    @Scheduled(cron = "0 */1 * * * *")
    public void processNotifications() {
        try {
            log.debug("일정 알림 발송 스케줄러 실행 시작");

            LocalDateTime now = LocalDateTime.now();
            LocalDate today = LocalDate.now();
            LocalDate endDate = today.plusDays(31); // 향후 31일 내 일정만 조회

            // 알림 발송 대상 일정 조회
            List<InfoSchedule> schedules = scheduleRepository.findSchedulesForNotification(now, today, endDate);

            log.debug("알림 발송 대상 일정 수: {}", schedules.size());

            // 현재 시간에 발송해야 하는 알림만 필터링 (비동기 처리)
            for (InfoSchedule schedule : schedules) {
                try {
                    List<NotificationTarget> targets = getNotificationTargetsForNow(schedule, now);
                    for (NotificationTarget target : targets) {
                        // 비동기로 알림 발송 (스케줄러 블로킹 방지)
                        notificationService.sendNotificationAsync(target.toServiceTarget());
                    }
                } catch (Exception e) {
                    log.error("일정 알림 처리 실패: scheduleId={}, error={}", 
                        schedule.getScheduleId(), e.getMessage(), e);
                }
            }

            log.debug("일정 알림 발송 스케줄러 실행 완료");
        } catch (Exception e) {
            log.error("일정 알림 스케줄러 실행 중 오류 발생", e);
        }
    }

    /**
     * 현재 시간에 발송해야 하는 알림 대상 목록 조회
     */
    private List<NotificationTarget> getNotificationTargetsForNow(InfoSchedule schedule, LocalDateTime now) {
        List<NotificationTarget> targets = new java.util.ArrayList<>();

        // 일정 시작 시간 계산
        LocalDateTime scheduleStartTime = schedule.getStartTime();
        if (scheduleStartTime == null) {
            scheduleStartTime = schedule.getStartDate().atStartOfDay();
        }

        // 일정 담당자 ID
        Integer assigneeId = schedule.getAssigneeId();
        if (assigneeId == null) {
            log.debug("일정 담당자가 없어 알림 발송 스킵: scheduleId={}", schedule.getScheduleId());
            return targets;
        }

        // 담당자의 알림 설정 조회
        InfoScheduleNotification notificationConfig = notificationRepository
            .findByUserIdAndEnabledTrue(assigneeId)
            .orElse(null);

        if (notificationConfig == null || !notificationConfig.getEmailEnabled()) {
            log.debug("알림 설정이 없거나 비활성화됨: userId={}", assigneeId);
            return targets;
        }

        // 1. 일정 유형별 알림 ON/OFF 우선 확인 (꺼져있으면 즉시 스킵)
        String scheduleTypeCode = schedule.getScheduleType() != null ? schedule.getScheduleType().getCode() : null;
        if (scheduleTypeCode != null && !isNotificationEnabledForScheduleType(notificationConfig, scheduleTypeCode)) {
            log.debug("해당 일정 유형 알림 비활성화: scheduleId={}, type={}", schedule.getScheduleId(), scheduleTypeCode);
            return targets;
        }

        // 2. 일정별 notificationTimings가 있으면 일정별 설정 사용, 없으면 전역 설정 사용
        List<?> notificationTimings;
        if (schedule.getNotificationTimings() != null && !schedule.getNotificationTimings().trim().isEmpty()) {
            notificationTimings = parseNotificationTimings(schedule.getNotificationTimings());
        } else {
            notificationTimings = parseNotificationTimings(notificationConfig.getNotificationTimings());
        }

        if (notificationTimings.isEmpty()) {
            log.debug("알림 시점 설정이 없음: scheduleId={}, userId={}", schedule.getScheduleId(), assigneeId);
            return targets;
        }

        // 3. 각 알림 시점별로 발송 시간 계산
        log.debug("알림 시점 목록: scheduleId={}, notificationTimings={}", schedule.getScheduleId(), notificationTimings);
        List<LocalDateTime> notificationTimes = timingCalculator.calculateNotificationTimes(
            scheduleStartTime,
            notificationTimings
        );
        log.debug("계산된 알림 시간: scheduleId={}, notificationTimes={}", schedule.getScheduleId(), notificationTimes);

        // 4. 현재 시간과 정확히 같은 분에 발송해야 하는 알림만 필터링
        for (int i = 0; i < notificationTimings.size() && i < notificationTimes.size(); i++) {
            Object notificationType = notificationTimings.get(i);
            LocalDateTime notificationTime = notificationTimes.get(i);

            // 현재 시간과 같은 분인지 확인 (년/월/일/시/분이 같으면 발송)
            if (notificationTime.getYear() == now.getYear() &&
                notificationTime.getMonth() == now.getMonth() &&
                notificationTime.getDayOfMonth() == now.getDayOfMonth() &&
                notificationTime.getHour() == now.getHour() &&
                notificationTime.getMinute() == now.getMinute()) {
                
                // 알림 발송 대상 생성
                String notificationTypeStr = formatNotificationType(notificationType);
                
                NotificationTarget target = new NotificationTarget(
                    schedule.getScheduleId(),
                    assigneeId,
                    notificationTypeStr,
                    notificationTime,
                    scheduleStartTime
                );

                targets.add(target);
            }
        }

        return targets;
    }

    /**
     * 알림 발송 대상 정보 (내부 클래스)
     */
    private static class NotificationTarget {
        private final Long scheduleId;
        private final Integer userId;
        private final String notificationType;
        private final LocalDateTime scheduledNotificationTime;
        private final LocalDateTime originalStartTime;

        public NotificationTarget(Long scheduleId, Integer userId, String notificationType,
                                LocalDateTime scheduledNotificationTime, LocalDateTime originalStartTime) {
            this.scheduleId = scheduleId;
            this.userId = userId;
            this.notificationType = notificationType;
            this.scheduledNotificationTime = scheduledNotificationTime;
            this.originalStartTime = originalStartTime;
        }

        public ScheduleNotificationService.NotificationTarget toServiceTarget() {
            return new ScheduleNotificationService.NotificationTarget(
                scheduleId,
                userId,
                notificationType,
                scheduledNotificationTime,
                originalStartTime
            );
        }
    }

    /**
     * 일정 유형별 알림 ON/OFF 확인
     */
    private boolean isNotificationEnabledForScheduleType(InfoScheduleNotification config, String scheduleTypeCode) {
        if (config.getScheduleTypeNotifications() == null || config.getScheduleTypeNotifications().isEmpty()) {
            return true; // 설정이 없으면 기본값: ON
        }

        try {
            Map<String, Boolean> typeNotifications = objectMapper.readValue(
                config.getScheduleTypeNotifications(), 
                new TypeReference<Map<String, Boolean>>() {}
            );

            // 설정에 해당 일정 유형이 없으면 기본값: ON
            return typeNotifications.getOrDefault(scheduleTypeCode, true);
        } catch (Exception e) {
            log.warn("일정 유형별 알림 설정 파싱 실패: scheduleTypeCode={}, error={}", 
                scheduleTypeCode, e.getMessage());
            return true; // 파싱 실패 시 기본값: ON
        }
    }

    /**
     * 알림 시점 목록 파싱 (문자열 형식 또는 JSON 형식 지원)
     * 문자열 형식: ["1일전", "3시간전"]
     * JSON 형식: [{"unit": "DAY", "value": 1}, {"unit": "HOUR", "value": 3}]
     */
    private List<?> parseNotificationTimings(String notificationTimingsJson) {
        if (notificationTimingsJson == null || notificationTimingsJson.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }

        try {
            // JSON 배열로 파싱 (문자열 또는 객체 모두 지원)
            return objectMapper.readValue(
                notificationTimingsJson, 
                new TypeReference<List<Object>>() {}
            );
        } catch (Exception e) {
            log.warn("알림 시점 파싱 실패: notificationTimings={}, error={}", 
                notificationTimingsJson, e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    /**
     * 알림 시점을 사람이 읽을 수 있는 형식으로 변환
     * DAY: {"unit":"DAY","date":"2025-12-19","hour":14,"minute":57} -> "2025-12-19 14:57"
     * HOUR: {"unit":"HOUR","hour":1,"minute":1} -> "1시간 1분 전"
     * MINUTE: {"unit":"MINUTE","minute":59} -> "59분 전"
     * 기존 형식: {"unit": "DAY", "value": 1} -> "1일 전"
     * 문자열: "1일전" -> "1일 전"
     */
    @SuppressWarnings("unchecked")
    private String formatNotificationType(Object notificationType) {
        if (notificationType == null) {
            return "";
        }

        // 문자열 형식인 경우 그대로 반환 (기존 형식: "1일전", "3시간전" 등)
        if (notificationType instanceof String) {
            String str = (String) notificationType;
            // "1일전" 형식을 "1일 전" 형식으로 변환 (가독성 향상)
            if (str.contains("일전")) {
                return str.replace("일전", "일 전");
            } else if (str.contains("시간전")) {
                return str.replace("시간전", "시간 전");
            } else if (str.contains("분전")) {
                return str.replace("분전", "분 전");
            }
            return str;
        }

        // Map 형식인 경우 파싱하여 변환
        if (notificationType instanceof Map) {
            try {
                Map<String, Object> timingMap = (Map<String, Object>) notificationType;
                String unitStr = (String) timingMap.get("unit");
                
                if (unitStr == null) {
                    log.warn("알림 시점 형식이 올바르지 않음: unit이 없음, timing={}", notificationType);
                    return notificationType.toString();
                }

                NotificationTimingUnit unit = NotificationTimingUnit.fromCode(unitStr);
                if (unit == null) {
                    log.warn("알 수 없는 알림 시점 단위: unit={}", unitStr);
                    return notificationType.toString();
                }

                Object dateObj = timingMap.get("date");
                Object hourObj = timingMap.get("hour");
                Object minuteObj = timingMap.get("minute");
                Object valueObj = timingMap.get("value");

                // DAY 형식: date, hour, minute이 모두 있는 경우 (해당 일자/시간에 알림)
                if (unit == NotificationTimingUnit.DAY && dateObj != null && hourObj != null && minuteObj != null) {
                    try {
                        String dateStr = dateObj.toString();
                        int hour = hourObj instanceof Number 
                            ? ((Number) hourObj).intValue() 
                            : Integer.parseInt(hourObj.toString());
                        int minute = minuteObj instanceof Number 
                            ? ((Number) minuteObj).intValue() 
                            : Integer.parseInt(minuteObj.toString());
                        
                        // "해당 일자 알림" 형식으로 표시
                        return String.format("해당 일자 알림 (%s %02d:%02d)", dateStr, hour, minute);
                    } catch (Exception e) {
                        log.warn("DAY 형식 알림 시점 변환 실패: notificationType={}, error={}", 
                            notificationType, e.getMessage());
                        return notificationType.toString();
                    }
                }

                // HOUR 형식: hour, minute이 있는 경우
                if (unit == NotificationTimingUnit.HOUR && hourObj != null && minuteObj != null) {
                    try {
                        int hour = hourObj instanceof Number 
                            ? ((Number) hourObj).intValue() 
                            : Integer.parseInt(hourObj.toString());
                        int minute = minuteObj instanceof Number 
                            ? ((Number) minuteObj).intValue() 
                            : Integer.parseInt(minuteObj.toString());
                        
                        if (minute > 0) {
                            return String.format("%d시간 %d분 전", hour, minute);
                        } else {
                            return String.format("%d시간 전", hour);
                        }
                    } catch (Exception e) {
                        log.warn("HOUR 형식 알림 시점 변환 실패: notificationType={}, error={}", 
                            notificationType, e.getMessage());
                        return notificationType.toString();
                    }
                }

                // MINUTE 형식: minute이 있는 경우
                if (unit == NotificationTimingUnit.MINUTE && minuteObj != null) {
                    try {
                        int minute = minuteObj instanceof Number 
                            ? ((Number) minuteObj).intValue() 
                            : Integer.parseInt(minuteObj.toString());
                        
                        return String.format("%d분 전", minute);
                    } catch (Exception e) {
                        log.warn("MINUTE 형식 알림 시점 변환 실패: notificationType={}, error={}", 
                            notificationType, e.getMessage());
                        return notificationType.toString();
                    }
                }

                // 기존 형식(하위 호환): value가 있는 경우
                if (valueObj != null) {
                    try {
                        int value = valueObj instanceof Number 
                            ? ((Number) valueObj).intValue() 
                            : Integer.parseInt(valueObj.toString());

                        return String.format("%d%s 전", value, unit.getLabel());
                    } catch (Exception e) {
                        log.warn("기존 형식 알림 시점 변환 실패: notificationType={}, error={}", 
                            notificationType, e.getMessage());
                        return notificationType.toString();
                    }
                }

                log.warn("알림 시점 형식이 올바르지 않음: unit={}, value={}, date={}, hour={}, minute={}", 
                    unitStr, valueObj, dateObj, hourObj, minuteObj);
                return notificationType.toString();
            } catch (Exception e) {
                log.warn("알림 시점 변환 실패: notificationType={}, error={}", notificationType, e.getMessage());
                return notificationType.toString();
            }
        }

        // 그 외의 경우 toString() 반환
        return notificationType.toString();
    }
}

