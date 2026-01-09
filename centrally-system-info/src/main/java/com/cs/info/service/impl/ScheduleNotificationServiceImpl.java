package com.cs.info.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cs.core.service.EmailService;
import com.cs.info.entity.InfoSchedule;
import com.cs.info.entity.InfoScheduleNotification;
import com.cs.info.entity.InfoScheduleNotificationLog;
import com.cs.info.repository.InfoScheduleNotificationLogRepository;
import com.cs.info.repository.InfoScheduleNotificationRepository;
import com.cs.info.service.InfoScheduleService;
import com.cs.info.service.ScheduleNotificationService;
import com.cs.info.util.NotificationTimingCalculator;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 일정 알림 발송 서비스 구현체
 */
@Service
@Slf4j
public class ScheduleNotificationServiceImpl implements ScheduleNotificationService {

    @Autowired
    private InfoScheduleService scheduleService;

    @Autowired
    private InfoScheduleNotificationRepository notificationRepository;

    @Autowired
    private InfoScheduleNotificationLogRepository logRepository;

    @Autowired
    private NotificationTimingCalculator timingCalculator;

    @Autowired
    private EmailService emailService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 알림 발송 (발송 직전 검증 포함) - 비동기
     */
    @Override
    @Async("mailExecutor")
    @Transactional
    public void sendNotificationAsync(NotificationTarget target) {
        sendNotification(target);
    }

    /**
     * 알림 발송 (발송 직전 검증 포함)
     */
    @Override
    @Transactional
    public void sendNotification(NotificationTarget target) {
        // 1. 일정 존재 및 활성화 여부 확인
        InfoSchedule schedule = scheduleService.getSchedule(target.getScheduleId());
        if (!isScheduleValid(schedule)) {
            log.warn("일정이 유효하지 않음: scheduleId={}", target.getScheduleId());
            saveNotificationLog(schedule, target, "EMAIL", false, "일정이 삭제되었거나 비활성화됨");
            return;
        }

        // 2. 일정 시간이 변경되지 않았는지 확인
        if (isScheduleTimeChanged(schedule, target)) {
            log.warn("일정 시간이 변경됨: scheduleId={}, 원래 시간={}, 현재 시간={}", 
                target.getScheduleId(), target.getOriginalStartTime(), schedule.getStartTime());
            saveNotificationLog(schedule, target, "EMAIL", false, "일정 시간이 변경됨");
            return;
        }

        // 3. 알림 설정 조회 및 유효성 확인
        InfoScheduleNotification config = getNotificationConfig(target.getUserId());
        if (config == null || !config.getEmailEnabled()) {
            log.warn("알림 설정이 없거나 비활성화됨: userId={}", target.getUserId());
            saveNotificationLog(schedule, target, "EMAIL", false, "알림 설정 비활성화");
            return;
        }

        // 4. 사용자 활성화 여부 확인 (HRM 서비스 연동 필요 - TODO)
        if (!isUserActive(target.getUserId())) {
            log.warn("사용자가 비활성화됨: userId={}", target.getUserId());
            saveNotificationLog(schedule, target, "EMAIL", false, "사용자 비활성화");
            return;
        }

        // 5. 중복 발송 방지 체크
        if (isAlreadySent(target)) {
            log.debug("이미 발송된 알림: scheduleId={}, userId={}, type={}", 
                target.getScheduleId(), target.getUserId(), target.getNotificationType());
            return;
        }

        // 6. 시간대 필터링 (평일 09:00~18:00 등)
        if (!isWithinNotificationTimeWindow(config, LocalDateTime.now())) {
            log.debug("알림 시간대가 아님: userId={}", target.getUserId());
            return;
        }

        // 7. 일정 유형별 알림 ON/OFF 확인
        if (!isNotificationEnabledForScheduleType(config, schedule.getScheduleType().getCode())) {
            log.debug("해당 일정 유형 알림 비활성화: scheduleId={}, type={}", 
                target.getScheduleId(), schedule.getScheduleType().getCode());
            return;
        }

        // 8. 최종 발송 직전 재확인 (Race Condition 방지)
        if (!revalidateBeforeSend(target, schedule, config)) {
            log.warn("발송 직전 재검증 실패: scheduleId={}", target.getScheduleId());
            return;
        }

        // 9. 이메일 발송
        try {
            sendEmailNotification(schedule, target);
            saveNotificationLog(schedule, target, "EMAIL", true, null);
            log.info("일정 알림 발송 완료: scheduleId={}, userId={}, type={}", 
                target.getScheduleId(), target.getUserId(), target.getNotificationType());
        } catch (Exception e) {
            log.error("알림 발송 실패: scheduleId={}, userId={}", 
                target.getScheduleId(), target.getUserId(), e);
            saveNotificationLog(schedule, target, "EMAIL", false, "발송 실패: " + e.getMessage());
        }
    }

    /**
     * 일정 유효성 검증
     */
    private boolean isScheduleValid(InfoSchedule schedule) {
        if (schedule == null) {
            return false;
        }

        // SoftDeleteEntity의 enabled, deletedAt 확인
        if (!schedule.getEnabled() || schedule.getDeletedAt() != null) {
            return false;
        }

        // 일정 시작 시간이 과거가 아닌지 확인
        LocalDateTime startTime = schedule.getStartTime();
        if (startTime == null) {
            startTime = schedule.getStartDate().atStartOfDay();
        }

        // 이미 시작된 일정은 알림 발송하지 않음
        if (startTime.isBefore(LocalDateTime.now())) {
            return false;
        }

        return true;
    }

    /**
     * 일정 시간 변경 여부 확인
     */
    private boolean isScheduleTimeChanged(InfoSchedule schedule, NotificationTarget target) {
        LocalDateTime currentStartTime = schedule.getStartTime();
        if (currentStartTime == null) {
            currentStartTime = schedule.getStartDate().atStartOfDay();
        }

        // 원래 저장된 시작 시간과 현재 일정의 시작 시간 비교
        LocalDateTime originalStartTime = target.getOriginalStartTime();
        if (originalStartTime == null) {
            return false; // 원래 시간 정보가 없으면 변경으로 간주하지 않음
        }

        // 시간이 변경되었는지 확인 (1분 이상 차이나면 변경으로 간주)
        return !currentStartTime.equals(originalStartTime) && 
               Math.abs(java.time.Duration.between(currentStartTime, originalStartTime).toMinutes()) >= 1;
    }

    /**
     * 발송 직전 최종 재검증 (Race Condition 방지)
     */
    @Transactional(readOnly = true)
    private boolean revalidateBeforeSend(NotificationTarget target, InfoSchedule schedule, InfoScheduleNotification config) {
        // 1. 일정 재조회 (최신 상태 확인)
        InfoSchedule latestSchedule = scheduleService.getSchedule(target.getScheduleId());
        if (!isScheduleValid(latestSchedule)) {
            return false;
        }

        // 2. 일정 시간 재확인
        if (isScheduleTimeChanged(latestSchedule, target)) {
            return false;
        }

        // 3. 알림 설정 재확인
        InfoScheduleNotification latestConfig = getNotificationConfig(target.getUserId());
        if (latestConfig == null || !latestConfig.getEmailEnabled()) {
            return false;
        }

        // 4. 중복 발송 재확인 (동시성 이슈 방지)
        if (isAlreadySent(target)) {
            return false;
        }

        return true;
    }

    /**
     * 사용자 활성화 여부 확인
     */
    private boolean isUserActive(Integer userId) {
        // 이메일이 저장되어 있으면 활성화된 사용자로 간주
        // 이메일 변경 시 Kafka를 통해 일괄 수정됨
        return true;
    }

    /**
     * 중복 발송 방지 (동시성 고려)
     */
    @Transactional(readOnly = true)
    private boolean isAlreadySent(NotificationTarget target) {
        // 발송 로그에서 동일한 알림이 이미 발송되었는지 확인
        Optional<InfoScheduleNotificationLog> existingLog = logRepository
            .findByScheduleIdAndUserIdAndNotificationTypeAndSent(
                target.getScheduleId(), 
                target.getUserId(), 
                target.getNotificationType(), 
                true
            );

        return existingLog.isPresent();
    }

    /**
     * 알림 설정 조회
     */
    private InfoScheduleNotification getNotificationConfig(Integer userId) {
        return notificationRepository.findByUserIdAndEnabledTrue(userId).orElse(null);
    }

    /**
     * 시간대 필터링 (평일 09:00~18:00)
     */
    private boolean isWithinNotificationTimeWindow(InfoScheduleNotification config, LocalDateTime now) {
        // 주말 체크
        int dayOfWeek = now.getDayOfWeek().getValue(); // 1=월요일, 7=일요일
        if (dayOfWeek == 6 || dayOfWeek == 7) { // 토요일, 일요일
            return config.getWeekendEnabled() != null && config.getWeekendEnabled();
        }

        // 평일 시간대 체크
        if (config.getWeekdayStartTime() != null && config.getWeekdayEndTime() != null) {
            try {
                String[] startParts = config.getWeekdayStartTime().split(":");
                String[] endParts = config.getWeekdayEndTime().split(":");
                
                int startHour = Integer.parseInt(startParts[0]);
                int startMinute = Integer.parseInt(startParts[1]);
                int endHour = Integer.parseInt(endParts[0]);
                int endMinute = Integer.parseInt(endParts[1]);

                LocalDateTime startTime = now.toLocalDate().atTime(startHour, startMinute);
                LocalDateTime endTime = now.toLocalDate().atTime(endHour, endMinute);

                return !now.isBefore(startTime) && !now.isAfter(endTime);
            } catch (Exception e) {
                log.warn("시간대 파싱 실패: startTime={}, endTime={}", 
                    config.getWeekdayStartTime(), config.getWeekdayEndTime());
                return true; // 파싱 실패 시 발송 허용
            }
        }

        return true; // 시간대 설정이 없으면 항상 허용
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
     * 이메일 알림 발송
     */
    private void sendEmailNotification(InfoSchedule schedule, NotificationTarget target) {
        // 일정 엔티티에서 담당자 이메일 조회
        String userEmail = schedule.getAssigneeEmail();
        
        if (userEmail == null || userEmail.trim().isEmpty()) {
            log.warn("사용자 이메일을 찾을 수 없음: scheduleId={}, userId={}", schedule.getScheduleId(), target.getUserId());
            throw new RuntimeException("사용자 이메일을 찾을 수 없습니다: scheduleId=" + schedule.getScheduleId());
        }

        String subject = String.format("[일정 알림] %s", schedule.getTitle());

        String message = String.format(
            "안녕하세요.\n\n" +
            "일정 알림입니다.\n\n" +
            "일정 제목: %s\n" +
            "일정 유형: %s\n" +
            "시작 시간: %s\n" +
            "알림 시점: %s\n\n" +
            "감사합니다.",
            schedule.getTitle(),
            schedule.getScheduleType().getLabel(),
            formatScheduleDateTime(schedule.getStartTime(), schedule.getStartDate()),
            target.getNotificationType()
        );

        emailService.sendSimpleMessage(userEmail, subject, message);
    }

    /**
     * 일정 시작 시간을 사람이 읽을 수 있는 형식으로 변환
     * 예: 2025-12-18T15:29 -> "2025년 12월 18일 15시 29분"
     *     LocalDate만 있는 경우: 2025-12-18 -> "2025년 12월 18일"
     */
    private String formatScheduleDateTime(LocalDateTime startTime, LocalDate startDate) {
        if (startTime != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 HH시 mm분", Locale.KOREAN);
            return startTime.format(formatter);
        } else if (startDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREAN);
            return startDate.format(formatter);
        }
        return "";
    }

    /**
     * 알림 발송 이력 저장
     * 일정 정보에서 사용자명과 이메일을 가져와서 함께 저장
     */
    private void saveNotificationLog(InfoSchedule schedule, NotificationTarget target, String channel, boolean sent, String failureReason) {
        InfoScheduleNotificationLog log = InfoScheduleNotificationLog.builder()
            .scheduleId(target.getScheduleId())
            .userId(target.getUserId())
            .userName(schedule != null ? schedule.getAssigneeName() : null)
            .userEmail(schedule != null ? schedule.getAssigneeEmail() : null)
            .notificationType(target.getNotificationType())
            .scheduledTime(target.getScheduledNotificationTime())
            .sentTime(sent ? LocalDateTime.now() : null)
            .channel(channel)
            .sent(sent)
            .failureReason(failureReason)
            .build();

        logRepository.save(log);
    }
}

