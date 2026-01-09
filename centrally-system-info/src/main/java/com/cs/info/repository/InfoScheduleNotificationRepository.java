package com.cs.info.repository;

import com.cs.info.entity.InfoScheduleNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 일정 알림 설정 Repository
 */
@Repository
public interface InfoScheduleNotificationRepository extends JpaRepository<InfoScheduleNotification, Long> {

    /**
     * 사용자 ID로 알림 설정 조회
     */
    Optional<InfoScheduleNotification> findByUserId(Integer userId);

    /**
     * 활성화된 사용자 알림 설정 조회
     */
    Optional<InfoScheduleNotification> findByUserIdAndEnabledTrue(Integer userId);

    /**
     * 사용자 ID로 알림 설정 존재 여부 확인
     */
    boolean existsByUserId(Integer userId);
}

