package com.cs.info.service;

import com.cs.info.entity.InfoScheduleNotification;

/**
 * 일정 알림 설정 서비스 인터페이스
 */
public interface InfoScheduleNotificationService {

    /**
     * 사용자별 알림 설정 조회
     * "일정 설정 페이지" 진입 및 "일정 생성 시" 알림 설정이 필요하므로, 설정이 없으면 기본 설정을 자동 생성하여 엔티티를 연결합니다.
     * - 유저가 처음 가입 시 일정 전역 설정에 대한 데이터 셋팅
     * @param userId 사용자 ID
     * @return 알림 설정 엔티티
     */
    InfoScheduleNotification getNotificationSettings(Integer userId);

    /**
     * 알림 설정 생성 또는 수정
     * @param userId 사용자 ID
     * @param notificationSettings 알림 설정 엔티티
     * @return 저장된 알림 설정 엔티티
     */
    InfoScheduleNotification saveNotificationSettings(Integer userId, InfoScheduleNotification notificationSettings);

    /**
     * 기본 알림 설정 생성
     * @param userId 사용자 ID
     * @return 생성된 알림 설정 엔티티
     */
    InfoScheduleNotification createDefaultNotificationSettings(Integer userId);

    /**
     * 알림 설정 삭제 (소프트 딜리트)
     * @param userId 사용자 ID
     */
    void deleteNotificationSettings(Integer userId);
}
