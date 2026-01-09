package com.cs.info.service.impl;

import com.cs.core.handler.GlobalExceptionHandler;
import com.cs.info.entity.InfoScheduleNotification;
import com.cs.info.repository.InfoScheduleNotificationRepository;
import com.cs.info.service.InfoScheduleNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 일정 알림 설정 서비스 구현체
 */
@Service
@Slf4j
public class InfoScheduleNotificationServiceImpl implements InfoScheduleNotificationService {

    private final InfoScheduleNotificationRepository notificationRepository;
    private final InfoScheduleNotificationService self;

    /**
     * 생성자 주입 (self-injection을 위해 @Lazy 사용)
     * 
     * @Lazy를 사용하는 이유:
     * 1. 순환 참조 방지: 자기 자신을 주입받을 때 순환 참조 문제를 해결
     * 2. 프록시를 통한 호출: @Lazy로 주입된 self는 프록시 객체이므로,
     *    같은 클래스 내에서 직접 호출해도 Spring AOP 프록시를 거쳐서 호출됨
     * 3. 트랜잭션 전파: getNotificationSettings()가 @Transactional(readOnly = true)인 상태에서
     *    self.createDefaultNotificationSettings()를 호출하면 @Transactional(propagation = REQUIRES_NEW)가
     *    제대로 작동하여 새로운 쓰기 트랜잭션이 생성됨
     */
    @Autowired
    public InfoScheduleNotificationServiceImpl(
            InfoScheduleNotificationRepository notificationRepository,
            @Lazy InfoScheduleNotificationService self) {
        this.notificationRepository = notificationRepository;
        this.self = self;
    }

    /**
     * 사용자별 알림 설정 조회
     * 
     * "일정 설정 페이지" 진입 및 "일정 생성 시" 알림 설정이 필요하므로, 설정이 없으면 기본 설정을 자동 생성하여 엔티티를 연결합니다.
     *  - 유저가 처음 가입 시 일정 전역 설정에 대한 데이터 셋팅
     * (프록시를 통해 호출하여 새 트랜잭션에서 실행)
     */
    @Override
    @Transactional(readOnly = true)
    public InfoScheduleNotification getNotificationSettings(Integer userId) {
        Optional<InfoScheduleNotification> existing = notificationRepository.findByUserIdAndEnabledTrue(userId);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // 처음 생성 시 알림 설정이 없으면 기본 설정을 자동 생성하여 엔티티 연결
        log.info("알림 설정이 없어 기본 설정을 자동 생성합니다: userId={}", userId);
        return self.createDefaultNotificationSettings(userId);
    }

    /**
     * 알림 설정 생성 또는 수정
     */
    @Override
    @Transactional
    public InfoScheduleNotification saveNotificationSettings(Integer userId, InfoScheduleNotification notificationSettings) {
        Optional<InfoScheduleNotification> existing = notificationRepository.findByUserId(userId);
        
        if (existing.isPresent()) {
            // 기존 설정 업데이트
            InfoScheduleNotification existingSettings = existing.get();
            existingSettings.setNotificationTimings(notificationSettings.getNotificationTimings());
            existingSettings.setTimezone(notificationSettings.getTimezone());
            existingSettings.setWeekdayStartTime(notificationSettings.getWeekdayStartTime());
            existingSettings.setWeekdayEndTime(notificationSettings.getWeekdayEndTime());
            existingSettings.setWeekendEnabled(notificationSettings.getWeekendEnabled());
            existingSettings.setHolidayEnabled(notificationSettings.getHolidayEnabled());
            existingSettings.setScheduleTypeNotifications(notificationSettings.getScheduleTypeNotifications());
            existingSettings.setEmailEnabled(notificationSettings.getEmailEnabled());
            existingSettings.setSmsEnabled(notificationSettings.getSmsEnabled());
            existingSettings.setPushEnabled(notificationSettings.getPushEnabled());
            
            return notificationRepository.save(existingSettings);
        } else {
            // 새 설정 생성
            notificationSettings.setUserId(userId);
            return notificationRepository.save(notificationSettings);
        }
    }

    /**
     * 기본 알림 설정 생성
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public InfoScheduleNotification createDefaultNotificationSettings(Integer userId) {
        // Race condition 방지: 새 트랜잭션에서 다시 확인
        Optional<InfoScheduleNotification> existing = notificationRepository.findByUserId(userId);
        if (existing.isPresent()) {
            log.warn("기본 알림 설정 생성 시도 중 이미 설정이 존재함: userId={}", userId);
            return existing.get();
        }

        InfoScheduleNotification defaultSettings = InfoScheduleNotification.builder()
                .userId(userId)
                .notificationTimings("[\"1일전\", \"1시간전\"]") // 기본값: 1일 전, 1시간 전
                .timezone("Asia/Seoul")
                .weekdayStartTime("09:00")
                .weekdayEndTime("18:00")
                .weekendEnabled(false)
                .holidayEnabled(false)
                .scheduleTypeNotifications("{}") // 모든 일정 유형 알림 ON
                .emailEnabled(true)
                .smsEnabled(false)
                .pushEnabled(false)
                .build();

        return notificationRepository.save(defaultSettings);
    }

    /**
     * 알림 설정 삭제 (소프트 딜리트)
     */
    @Override
    @Transactional
    public void deleteNotificationSettings(Integer userId) {
        InfoScheduleNotification settings = notificationRepository.findByUserIdAndEnabledTrue(userId)
                .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "알림 설정을 찾을 수 없습니다: userId=" + userId));
        
        notificationRepository.delete(settings);
    }
}

