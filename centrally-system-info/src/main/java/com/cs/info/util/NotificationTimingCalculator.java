package com.cs.info.util;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.cs.info.enums.NotificationTimingUnit;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 알림 시점 계산 유틸리티
 */
@Component
@Slf4j
public class NotificationTimingCalculator {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 일정 시작 시간 기준으로 알림 발송 시간 계산
     * 
     * @param scheduleStartTime 일정 시작 시간
     * @param notificationTimings 알림 시점 목록 (JSON 형식: [{"unit": "DAY", "value": 1}])
     * @return 알림 발송 시간 목록
     */
    public List<LocalDateTime> calculateNotificationTimes(
            LocalDateTime scheduleStartTime, 
            List<?> notificationTimings) {
        
        List<LocalDateTime> notificationTimes = new ArrayList<>();
        
        if (scheduleStartTime == null || notificationTimings == null || notificationTimings.isEmpty()) {
            return notificationTimes;
        }
        
        for (Object timing : notificationTimings) {
            try {
                LocalDateTime notificationTime = parseTiming(scheduleStartTime, timing);
                if (notificationTime != null) {
                    // 알림 시간이 일정 시작 시간 이전이어야 함
                    if (notificationTime.isBefore(scheduleStartTime)) {
                        notificationTimes.add(notificationTime);
                    } else {
                        log.debug("알림 시간이 일정 시작 시간 이후이므로 제외: notificationTime={}, scheduleStartTime={}", 
                            notificationTime, scheduleStartTime);
                    }
                }
            } catch (Exception e) {
                log.warn("알림 시점 파싱 실패: timing={}, error={}", timing, e.getMessage());
            }
        }
        
        return notificationTimes;
    }

    /**
     * 알림 시점 파싱
     * 
     * DAY 형식: {"unit":"DAY","date":"2025-12-19","hour":14,"minute":57} - 해당 일자/시간에 알림
     * HOUR 형식: {"unit":"HOUR","hour":1,"minute":1} - 일정 시작 시간에서 1시간 1분 전
     * MINUTE 형식: {"unit":"MINUTE","minute":59} - 일정 시작 시간에서 59분 전
     * 
     * 기존 형식(하위 호환): {"unit": "DAY", "value": 1} - 일정 시작 시간에서 value만큼 뺀 시간
     * 
     * @param scheduleStart 일정 시작 시간
     * @param timing 알림 시점 (Map 또는 JSON 문자열)
     * @return 알림 발송 시간
     */
    @SuppressWarnings("unchecked")
    public LocalDateTime parseTiming(LocalDateTime scheduleStart, Object timing) {
        if (scheduleStart == null || timing == null) {
            return null;
        }

        Map<String, Object> timingMap = null;

        // Map인 경우 직접 사용
        if (timing instanceof Map) {
            timingMap = (Map<String, Object>) timing;
        } 
        // JSON 문자열인 경우 파싱
        else if (timing instanceof String && timing.toString().trim().startsWith("{")) {
            try {
                timingMap = objectMapper.readValue(
                    timing.toString(), 
                    new TypeReference<Map<String, Object>>() {}
                );
            } catch (Exception e) {
                log.warn("JSON 문자열 파싱 실패: timing={}, error={}", timing, e.getMessage());
                return null;
            }
        } else {
            log.warn("지원하지 않는 알림 시점 형식: timing={}", timing);
            return null;
        }

        String unitStr = (String) timingMap.get("unit");
        if (unitStr == null) {
            log.warn("알림 시점 형식이 올바르지 않음: unit이 없음, timing={}", timing);
            return null;
        }

        try {
            NotificationTimingUnit unit = NotificationTimingUnit.fromCode(unitStr);
            if (unit == null) {
                log.warn("알 수 없는 알림 시점 단위: unit={}", unitStr);
                return null;
            }

            Object dateObj = timingMap.get("date");
            Object hourObj = timingMap.get("hour");
            Object minuteObj = timingMap.get("minute");
            Object valueObj = timingMap.get("value");

            // 디버깅: 파싱된 값 확인
            log.debug("알림 시점 파싱: unit={}, date={}, hour={}, minute={}, value={}", 
                unitStr, dateObj, hourObj, minuteObj, valueObj);

            // DAY 형식: date, hour, minute이 모두 있는 경우 (해당 일자/시간에 알림)
            if (unit == NotificationTimingUnit.DAY) {
                // date, hour, minute이 모두 있는지 확인 (null이 아니고 빈 문자열이 아닌지)
                boolean hasDate = dateObj != null && !dateObj.toString().trim().isEmpty();
                boolean hasHour = hourObj != null;
                boolean hasMinute = minuteObj != null;
                
                if (hasDate && hasHour && hasMinute) {
                    try {
                        String dateStr = dateObj.toString().trim();
                        int hour = hourObj instanceof Number 
                            ? ((Number) hourObj).intValue() 
                            : Integer.parseInt(hourObj.toString().trim());
                        int minute = minuteObj instanceof Number 
                            ? ((Number) minuteObj).intValue() 
                            : Integer.parseInt(minuteObj.toString().trim());
                        
                        // 날짜 파싱 (yyyy-MM-dd 형식)
                        java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
                        LocalDateTime result = LocalDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), hour, minute);
                        log.debug("DAY 형식 알림 시점 파싱 성공: date={}, hour={}, minute={}, result={}", dateStr, hour, minute, result);
                        return result;
                    } catch (Exception e) {
                        log.warn("DAY 형식 알림 시점 파싱 실패: timing={}, date={}, hour={}, minute={}, error={}", 
                            timing, dateObj, hourObj, minuteObj, e.getMessage());
                        return null;
                    }
                } else {
                    log.debug("DAY 형식이지만 필수 필드가 없음: date={}, hour={}, minute={}", dateObj, hourObj, minuteObj);
                }
            }
            
            // HOUR 형식: hour, minute이 있는 경우 (일정 시작 시간에서 hour시간 minute분 전)
            if (unit == NotificationTimingUnit.HOUR && hourObj != null && minuteObj != null) {
                try {
                    int hour = hourObj instanceof Number 
                        ? ((Number) hourObj).intValue() 
                        : Integer.parseInt(hourObj.toString());
                    int minute = minuteObj instanceof Number 
                        ? ((Number) minuteObj).intValue() 
                        : Integer.parseInt(minuteObj.toString());
                    
                    return scheduleStart.minusHours(hour).minusMinutes(minute);
                } catch (Exception e) {
                    log.warn("HOUR 형식 알림 시점 파싱 실패: timing={}, error={}", timing, e.getMessage());
                    return null;
                }
            }
            
            // MINUTE 형식: minute이 있는 경우 (일정 시작 시간에서 minute분 전)
            if (unit == NotificationTimingUnit.MINUTE && minuteObj != null) {
                try {
                    int minute = minuteObj instanceof Number 
                        ? ((Number) minuteObj).intValue() 
                        : Integer.parseInt(minuteObj.toString());
                    
                    return scheduleStart.minusMinutes(minute);
                } catch (Exception e) {
                    log.warn("MINUTE 형식 알림 시점 파싱 실패: timing={}, error={}", timing, e.getMessage());
                    return null;
                }
            }
            
            // 기존 형식(하위 호환): value가 있는 경우
            if (valueObj != null) {
                try {
                    int value = valueObj instanceof Number 
                        ? ((Number) valueObj).intValue() 
                        : Integer.parseInt(valueObj.toString());
                    
                    if (unit == NotificationTimingUnit.DAY) {
                        return scheduleStart.minusDays(value);
                    } else if (unit == NotificationTimingUnit.HOUR) {
                        return scheduleStart.minusHours(value);
                    } else if (unit == NotificationTimingUnit.MINUTE) {
                        return scheduleStart.minusMinutes(value);
                    }
                } catch (Exception e) {
                    log.warn("기존 형식 알림 시점 파싱 실패: timing={}, error={}", timing, e.getMessage());
                    return null;
                }
            }
            
            log.warn("알림 시점 형식이 올바르지 않음: unit={}, value={}, date={}, hour={}, minute={}", 
                unitStr, valueObj, dateObj, hourObj, minuteObj);
            return null;
            
        } catch (Exception e) {
            log.warn("알림 시점 파싱 실패: timing={}, error={}", timing, e.getMessage());
            return null;
        }
    }

}

