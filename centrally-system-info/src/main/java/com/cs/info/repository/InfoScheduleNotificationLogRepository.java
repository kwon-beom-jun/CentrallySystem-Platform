package com.cs.info.repository;

import com.cs.info.entity.InfoScheduleNotificationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 일정 알림 발송 이력 Repository
 */
@Repository
public interface InfoScheduleNotificationLogRepository extends JpaRepository<InfoScheduleNotificationLog, Long> {

    /**
     * 일정 ID, 사용자 ID, 알림 유형, 발송 성공 여부로 조회 (중복 발송 방지용)
     */
    Optional<InfoScheduleNotificationLog> findByScheduleIdAndUserIdAndNotificationTypeAndSent(
            Long scheduleId, 
            Integer userId, 
            String notificationType, 
            Boolean sent
    );

    /**
     * 일정 ID로 발송 이력 조회
     */
    java.util.List<InfoScheduleNotificationLog> findByScheduleId(Long scheduleId);

    /**
     * 사용자 ID로 발송 이력 조회
     */
    java.util.List<InfoScheduleNotificationLog> findByUserId(Integer userId);

    /**
     * 날짜 범위와 사용자 ID로 페이징 조회
     * @param startDate 시작 날짜 (필수)
     * @param endDate 종료 날짜 (필수)
     * @param userId 사용자 ID (null이면 무시)
     * @param pageable 페이징 정보
     * @return 페이징된 알림 발송 이력 목록
     */
    @Query("SELECT log FROM InfoScheduleNotificationLog log " +
           "WHERE log.scheduledTime >= :startDate " +
           "AND log.scheduledTime <= :endDate " +
           "AND (:userId IS NULL OR log.userId = :userId) " +
           "ORDER BY log.scheduledTime DESC")
    Page<InfoScheduleNotificationLog> findByDateRangeAndUserId(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("userId") Integer userId,
            Pageable pageable
    );

    /**
     * 날짜 범위와 사용자명/이메일로 페이징 조회
     * @param startDate 시작 날짜 (필수)
     * @param endDate 종료 날짜 (필수)
     * @param searchKeyword 검색어 (사용자명 또는 이메일)
     * @param pageable 페이징 정보
     * @return 페이징된 알림 발송 이력 목록
     */
    @Query("SELECT log FROM InfoScheduleNotificationLog log " +
           "WHERE log.scheduledTime >= :startDate " +
           "AND log.scheduledTime <= :endDate " +
           "AND (LOWER(COALESCE(log.userName, '')) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR " +
           "     LOWER(COALESCE(log.userEmail, '')) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) " +
           "ORDER BY log.scheduledTime DESC")
    Page<InfoScheduleNotificationLog> findByDateRangeAndUserNameOrEmail(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("searchKeyword") String searchKeyword,
            Pageable pageable
    );
}

